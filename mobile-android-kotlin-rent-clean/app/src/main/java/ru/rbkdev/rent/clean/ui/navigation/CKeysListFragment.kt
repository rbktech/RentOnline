package ru.rbkdev.rent.clean.ui.navigation

import ru.rbkdev.rent.clean.R
import ru.rbkdev.rent.clean.room.CDatabaseViewModel

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import java.util.*

/***/
class CKeysListFragment : Fragment() {

    /** BACKEND */
    // -------------------------------------------------------------------

    private var mTimer: Timer? = null
    private var mAdapter: CBarcodeListAdapter? = null
    private var mViewModelDatabase: CDatabaseViewModel? = null

    /***/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.keys_list_fragment, container, false)

        /** GUI */
        val recyclerView = view.findViewById<RecyclerView>(R.id.rcvKeysList)

        /** Settings view module database */
        mViewModelDatabase = ViewModelProvider(this).get(CDatabaseViewModel::class.java)

        mTimer = Timer()

        /** Settings adapter */
        mAdapter = CBarcodeListAdapter(mViewModelDatabase)

        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        mViewModelDatabase?.let { viewModule ->
            mAdapter?.let { adapter ->
                viewModule.getListKeys().observe(requireActivity()) { list ->
                    adapter.submitList(list)
                }
            }
        }

        mTimer?.schedule(DeleteHousePastTense(), 0, 1000)

        return view
    }

    /***/
    override fun onPause() {
        super.onPause()

        mTimer?.cancel()
    }

    inner class DeleteHousePastTense : TimerTask() {

        override fun run() {

            val currentTime = System.currentTimeMillis() / 1000

            mAdapter?.currentList?.forEach { house ->

                if (house.timeEnd.toLong() < currentTime)
                    mViewModelDatabase?.delete(requireContext(), house)
            }
        }
    }
}