package ru.rbkdev.lease.search.result.pageradapter.listfarm;
import ru.rbkdev.lease.R;
import ru.rbkdev.lease.account.login.CLoginActivity;
import ru.rbkdev.lease.main.CClient;
import ru.rbkdev.lease.main.CException;
import ru.rbkdev.lease.main.CFarm;
import ru.rbkdev.lease.main.CMainActivity;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.view.ViewGroup;
import android.content.Intent;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.view.LayoutInflater;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.util.Set;

import ru.yoomoney.sdk.kassa.payments.Checkout;
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.Amount;
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentMethodType;
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.PaymentParameters;
import ru.yoomoney.sdk.kassa.payments.checkoutParameters.SavePaymentMethod;

public class CRecyclerAdapter extends RecyclerView.Adapter<CRecyclerAdapter.CFarmViewHolder>
{
    private static boolean m_owner; // true - owner farms, false - all list farms
    private final List<CFarm> m_listFarm;
    private final Context m_context;
    private final Fragment m_fragment;

    public CRecyclerAdapter(boolean owner, Context context, Fragment fragment) {
        m_owner = owner;
        m_context = context;
        m_fragment = fragment;
        m_listFarm = new ArrayList<>();
    }

    @NotNull
    @Override
    public CFarmViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int viewType)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ferm, viewGroup, false);
        return new CFarmViewHolder(view);
    }

    public static class CFarmViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView m_street;
        private final TextView m_house;
        private final TextView m_rating;
        private final TextView m_distance;
        private final TextView m_number_rooms;
        private final TextView m_price;

        private final ImageView m_imgFarm;

        private final Button m_btnPayDel;

        private final LinearLayout m_layoutFarm;

        public CFarmViewHolder(View itemView)
        {
            super (itemView);

            m_layoutFarm = itemView.findViewById(R.id.layoutFarm);

            m_street = itemView.findViewById(R.id.txtStreet);
            m_house = itemView.findViewById(R.id.txtHouse);
            m_rating = itemView.findViewById(R.id.txtRating);
            m_distance = itemView.findViewById(R.id.txtDistance);
            m_number_rooms = itemView.findViewById(R.id.txtNumberRooms);
            m_price = itemView.findViewById(R.id.txtPrice);

            m_imgFarm = itemView.findViewById(R.id.imgFerm);

            m_btnPayDel = itemView.findViewById(R.id.btnPayDel);
            ImageButton m_btnLike = itemView.findViewById(R.id.btnLike);

            if(!m_owner) {
                if(CMainActivity.g_userId.charAt(0) == 'A')
                    m_btnPayDel.setText("Авторизация");
                else
                    m_btnPayDel.setText("Арендовать");
                m_btnLike.setVisibility(View.VISIBLE);
            } else {
                m_btnPayDel.setText("Удалить");
                m_btnLike.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void onItemDel(int position)
    {
        if(position != -1 && position < m_listFarm.size()) {
            m_listFarm.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        }
    }

    public void onClear()
    {
        int listSize = m_listFarm.size();
        for(int i = 0; i < listSize; i++)
            onItemDel(0);
    }

    public void onItemArray(@NotNull ArrayList<ByteArrayOutputStream> data) {
        int position = 1;
        CFarm farm = new CFarm();
        farm.m_id = data.get(position++).toString();

        if(data.get(position++).toString().equals("111"))
            farm.m_town = "Москва";

        farm.m_street = data.get(position++).toString();
        farm.m_house = data.get(position++).toString();
        farm.m_rating = data.get(position++).toString();
        farm.m_distance = data.get(position++).toString();
        farm.m_number_rooms = data.get(position++).toString();
        farm.m_price = data.get(position++).toString();
        farm.m_photo = data.get(position).toByteArray();

        m_listFarm.add(0, farm);
        notifyItemInserted(0);
        // notifyItemRangeInserted(m_lstFarm.size() + 1, m_lstFarm.size());
    }

    @Override
    public int getItemCount() { return m_listFarm.size(); }

    @Override
    public void onBindViewHolder(@NotNull CFarmViewHolder viewHolder, final int position) {
        viewHolder.m_street.setText(m_listFarm.get(position).m_street);
        viewHolder.m_house.setText(m_listFarm.get(position).m_house);
        viewHolder.m_rating.setText(m_listFarm.get(position).m_rating);
        viewHolder.m_distance.setText(m_listFarm.get(position).m_distance);
        viewHolder.m_number_rooms.setText(m_listFarm.get(position).m_number_rooms);
        viewHolder.m_price.setText(m_listFarm.get(position).m_price);
        viewHolder.m_imgFarm.setImageBitmap(BitmapFactory.decodeByteArray(m_listFarm.get(position).m_photo, 0, m_listFarm.get(position).m_photo.length));

        viewHolder.m_btnPayDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommandPayFarm(position);
            }
        });

        viewHolder.m_layoutFarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommandReadFarm(position);
            }
        });
    }

    // Command "P" : Оплата покупки
    private void CommandPayFarm(int position) {

        if(CMainActivity.g_userId.charAt(0) != 'A') {

            String street = m_listFarm.get(position).m_street;
            String house = m_listFarm.get(position).m_house;
            String farm_id = m_listFarm.get(position).m_id;
            String price = m_listFarm.get(position).m_price;

            String begin = ((CListFarmFragment)m_fragment).m_dateBegin;
            String end = ((CListFarmFragment)m_fragment).m_dateEnd;

            String title = "Москва, ул. " + street + " д. " + house;
            String subtitle = "Аренда: c " + begin + " до " + end;

            Set<PaymentMethodType> paymentMethodTypes = new HashSet<>();
            paymentMethodTypes.add(PaymentMethodType.BANK_CARD);
            paymentMethodTypes.add(PaymentMethodType.SBERBANK);
            paymentMethodTypes.add(PaymentMethodType.GOOGLE_PAY);

            PaymentParameters paymentParameters = new PaymentParameters(
                    new Amount(new BigDecimal(price), Currency.getInstance("RUB")),
                    title,
                    subtitle,
                    "ya_token",
                    "ya_id",
                    SavePaymentMethod.OFF,
                    paymentMethodTypes
            );

            Intent intentToken = Checkout.createTokenizeIntent(m_context, paymentParameters);
            m_fragment.startActivityForResult(intentToken, CListFarmFragment.REQUEST_CODE_TOKENIZE);

            Intent intentSend = ((Activity)m_context).getIntent();
            intentSend.putExtra("farm_id", farm_id.getBytes());
            ((Activity) m_context).setResult(Activity.RESULT_OK, intentSend);

        } else
            m_context.startActivity(new Intent(m_context, CLoginActivity.class));
    }

    // Command "R" : Чтение квартиры
    private void CommandReadFarm(int position) {

        ArrayList<byte[]> data = new ArrayList<>();
        data.add("111".getBytes());
        data.add(m_listFarm.get(position).m_id.getBytes());

        ArrayList<ArrayList<ByteArrayOutputStream>> answer = CClient.DataExchange(data, (byte)0x52, m_context);
        if(answer != null) {
            try {
                int min_size = 4;

                ArrayList<ByteArrayOutputStream> item = answer.get(1);
                int sizeBundle = item.size();
                Intent intent = new Intent(m_context, CViewFarmActivity.class);

                if(sizeBundle < min_size)
                    throw new CException("Ошибка парсера");

                intent.putExtra("town", m_listFarm.get(position).m_town);
                intent.putExtra("street", m_listFarm.get(position).m_street);
                intent.putExtra("house", m_listFarm.get(position).m_house);
                intent.putExtra("owner", item.get(1).toString());
                intent.putExtra("rating", m_listFarm.get(position).m_rating);
                intent.putExtra("distance", m_listFarm.get(position).m_distance);
                intent.putExtra("number_rooms", m_listFarm.get(position).m_number_rooms);
                intent.putExtra("geo", item.get(2).toString());
                intent.putExtra("description", item.get(3).toString());
                intent.putExtra("photo_0", m_listFarm.get(position).m_photo);

                int number_photo = Integer.parseInt(item.get(min_size).toString());
                if(sizeBundle < min_size + number_photo)
                    throw new CException("Ошибка парсера");

                intent.putExtra("number_photo", number_photo);
                for(int i = 1; i < number_photo; i++)
                    intent.putExtra("photo_" + i, item.get(min_size + i).toByteArray());

                min_size += number_photo;

                int number_comments = Integer.parseInt(item.get(min_size).toString());
                if(sizeBundle < min_size + number_comments)
                    throw new CException("Ошибка парсера");

                intent.putExtra("number_comments", number_comments);
                for(int i = 0; i < number_comments; i++)
                    intent.putExtra("comments_" + i, item.get(min_size + i).toByteArray());

                m_context.startActivity(intent);

            } catch(CException error) {
                Toast.makeText(m_context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(m_context, R.string.not_connection, Toast.LENGTH_SHORT).show();
    }
}