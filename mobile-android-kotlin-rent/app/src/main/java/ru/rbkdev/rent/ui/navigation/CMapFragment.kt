package ru.rbkdev.rent.ui.navigation

import ru.rbkdev.rent.R

import ru.rbkdev.rent.CClient
import ru.rbkdev.rent.CListExchange
import ru.rbkdev.rent.CSettings
import ru.rbkdev.rent.ui.calendar.CCalendarActivity
import ru.rbkdev.rent.room.database.keys.CKeysTable

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView

import androidx.fragment.app.Fragment

import com.google.android.material.bottomsheet.BottomSheetDialog

import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.GeoObjectTapEvent
import com.yandex.mapkit.layers.GeoObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

import kotlin.math.abs
import kotlin.math.sqrt

/***/
class CMapFragment : Fragment(), ClusterListener, GeoObjectTapListener, InputListener {

    private var mMapView: MapView? = null

    private var mBottomSheetSelectHouse: BottomSheetDialog? = null

    companion object {

        private const val FONT_SIZE = 15f
        private const val MARGIN_SIZE = 3f
        private const val STROKE_SIZE = 3f
    }

    private inner class TextImageProvider(private val text: String) : ImageProvider() {
        override fun getId(): String {
            return "text_$text"
        }

        override fun getImage(): Bitmap {
            val metrics = DisplayMetrics()

            activity?.windowManager?.defaultDisplay?.getMetrics(metrics)

            val textPaint = Paint()
            textPaint.textSize = FONT_SIZE * metrics.density
            textPaint.textAlign = Align.CENTER
            textPaint.style = Paint.Style.FILL
            textPaint.isAntiAlias = true
            val widthF = textPaint.measureText(text)
            val textMetrics = textPaint.fontMetrics
            val heightF = abs(textMetrics.bottom) + abs(textMetrics.top)
            val textRadius = sqrt((widthF * widthF + heightF * heightF).toDouble()).toFloat() / 2
            val internalRadius: Float = textRadius + MARGIN_SIZE * metrics.density
            val externalRadius: Float = internalRadius + STROKE_SIZE * metrics.density
            val width = (2 * externalRadius + 0.5).toInt()
            val bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val backgroundPaint = Paint()
            backgroundPaint.isAntiAlias = true
            backgroundPaint.color = Color.RED
            canvas.drawCircle((width / 2).toFloat(), (width / 2).toFloat(), externalRadius, backgroundPaint)
            backgroundPaint.color = Color.WHITE
            canvas.drawCircle((width / 2).toFloat(), (width / 2).toFloat(), internalRadius, backgroundPaint)
            canvas.drawText(text, (width / 2).toFloat(), width / 2 - (textMetrics.ascent + textMetrics.descent) / 2, textPaint)
            return bitmap
        }
    }

    /***/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        MapKitFactory.initialize(requireContext())

        val view = inflater.inflate(R.layout.main_map_fragment, container, false)

        mBottomSheetSelectHouse = BottomSheetDialog(requireContext())

        mMapView = view.findViewById(R.id.mapview) as MapView

        // mMapView?.map?.move(CameraPosition(CLUSTER_CENTERS[0], 3F, 0F, 0F))
        mMapView?.map?.move(
            CameraPosition(Point(55.751574, 37.573856), 11.0F, 0.0F, 0.0F),
            Animation(Animation.Type.SMOOTH, 0F),
            null
        )

        val imageProvider = ImageProvider.fromResource(requireContext(), R.drawable.ic_home)

        val clusteredCollection: ClusterizedPlacemarkCollection? = mMapView?.map?.mapObjects?.addClusterizedPlacemarkCollection(this)

        val listExchange = CListExchange()

        listExchange.setAddress(CSettings.getInstance().getAddressLock(requireContext()))
        listExchange.setIdUser(CSettings.getInstance().getUserId())
        listExchange.setIdHouse("0")
        listExchange.setCommand("get_lock_list")
        CClient.startExchange(requireContext(), listExchange)
        val list = listExchange.getList()
        if (list != null && list.isNotEmpty()) {

            var house: CKeysTable?

            house = CKeysTable()
            house.addressHouse = list[0].addressHouse
            house.idHouse = list[0].idHouse
            house.latitude = 55.757105
            house.longitude = 37.608766

            var mark = clusteredCollection?.addPlacemark(Point(house.latitude, house.longitude), imageProvider, IconStyle())
            mark?.userData = house

            house = CKeysTable()
            house.addressHouse = "A2"
            house.idHouse = "33"
            house.latitude = 55.755591
            house.longitude = 37.641854

            mark = clusteredCollection?.addPlacemark(Point(house.latitude, house.longitude), imageProvider, IconStyle())
            mark?.userData = house
        }

        clusteredCollection?.clusterPlacemarks(60.0, 15)

        clusteredCollection?.addTapListener(circleMapObjectTapListener)

        mMapView?.map?.addTapListener(this)
        mMapView?.map?.addInputListener(this)

        return view
    }

    /***/
    override fun onStop() {
        mMapView?.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    /***/
    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mMapView?.onStart()
    }

    /***/
    override fun onClusterAdded(cluster: Cluster) {
        cluster.appearance.setIcon(TextImageProvider("${cluster.size}"))
    }

    private val circleMapObjectTapListener = MapObjectTapListener { mapObject, _ ->

        if (mapObject is PlacemarkMapObject) {
            val userData = mapObject.userData
            if (userData is CKeysTable) {

                mBottomSheetSelectHouse?.let { bottomSheet ->

                    if (!bottomSheet.isShowing) {
                        val view = layoutInflater.inflate(R.layout.main_map_select_house_bottom_sheet, null as ViewGroup?)

                        val btnSelectHouse = view.findViewById<Button>(R.id.btnMainMapSelectHouse)
                        val txtSelectHouse = view.findViewById<TextView>(R.id.txtMainMapSelectHouse)

                        txtSelectHouse.text = userData.addressHouse

                        btnSelectHouse.setOnClickListener {

                            val intent = Intent(requireContext(), CCalendarActivity::class.java)
                            intent.putExtra(getString(R.string.intent_house), userData)
                            startActivity(intent)

                            bottomSheet.dismiss()
                        }

                        // bottomSheet.setCancelable(true)
                        bottomSheet.setContentView(view)
                        bottomSheet.show()
                    }
                }
            }
        }

        true
    }

    /** Objects tap */

    override fun onObjectTap(geoObjectTapEvent: GeoObjectTapEvent): Boolean {

        val selectionMetadata = geoObjectTapEvent.geoObject.metadataContainer.getItem(GeoObjectSelectionMetadata::class.java)
        if (selectionMetadata != null) {
            mMapView?.map?.let {

                it.selectGeoObject(selectionMetadata.id, selectionMetadata.layerId)
                return true
            }
        }

        return false
    }

    /***/
    override fun onMapTap(map: Map, point: Point) {
        mMapView?.map?.deselectGeoObject()
    }

    /***/
    override fun onMapLongTap(map: Map, point: Point) {}
}