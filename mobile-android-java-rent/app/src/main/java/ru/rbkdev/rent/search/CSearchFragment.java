package ru.rbkdev.rent.search;
import ru.rbkdev.rent.search.result.CSearchActivity;
import ru.rbkdev.rent.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CSearchFragment  extends Fragment {

    private Context m_context;

    private Button m_btnNumberPerson;
    private Button m_btnDate;
    private Button m_btnTown;

    DateFormat m_formatter;
    private String m_dateBegin;
    private String m_dateEnd;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search, container, false);
        m_context = getContext();

        m_btnTown = view.findViewById(R.id.btnTown);
        m_btnDate = view.findViewById(R.id.btnDate);
        m_btnNumberPerson = view.findViewById(R.id.btnNumberPerson);

        m_btnTown.setOnClickListener(OpenBottomSheetDialogTown);
        m_btnDate.setOnClickListener(OpenBottomSheetDialogCalendar);
        m_btnNumberPerson.setOnClickListener(OpenBottomSheetDialogNumberPerson);

        m_formatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        m_formatter.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));

        Date date = Calendar.getInstance().getTime();
        m_dateBegin = m_formatter.format(date);
        m_dateEnd = m_formatter.format(new Date(date.getTime() + 86400000));
        m_btnDate.setText((m_dateBegin + " - " + m_dateEnd));

        Button btnSearch = view.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Date date;
                    String dateBeginSec;
                    String dateEndSec;

                    date = m_formatter.parse(m_dateBegin);
                    if(date != null)
                        dateBeginSec = ((date.getTime() + 43200000) / 1000) + ""; // + 12  часов
                    else
                        return;

                    date = m_formatter.parse(m_dateEnd);
                    if(date != null)
                        dateEndSec = ((date.getTime() + 43200000) / 1000) + "";
                    else
                        return;

                    Intent intent = new Intent(getActivity(), CSearchActivity.class);
                    intent.putExtra("town", "111");
                    intent.putExtra("number_person", m_btnNumberPerson.getText().toString());
                    intent.putExtra("data_begin", m_dateBegin);
                    intent.putExtra("data_end", m_dateEnd);
                    intent.putExtra("data_begin_sec", dateBeginSec);
                    intent.putExtra("data_end_sec", dateEndSec);
                    startActivity(intent);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        return view;
    }

    View.OnClickListener OpenBottomSheetDialogNumberPerson = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(m_context, R.style.BottomSheetDialogTheme);
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(m_context);

            int layout = R.layout.layout_numberperson;
            LinearLayout linearLayout = v.findViewById(R.id.llNumberPerson);
            View bottomSheetView = LayoutInflater.from(getContext()).inflate(layout, linearLayout);

            final EditText txtPerson = bottomSheetView.findViewById(R.id.txtNumberPersonSearch);

            bottomSheetView.findViewById(R.id.btnSaveNumberPerson).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_btnNumberPerson.setText(txtPerson.getText().toString());
                    bottomSheetDialog.dismiss();
                }
            });

            bottomSheetView.findViewById(R.id.btnPlus).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int count = Integer.parseInt(txtPerson.getText().toString());
                    count++;
                    txtPerson.setText(String.valueOf(count));
                }
            });

            bottomSheetView.findViewById(R.id.btnMinus).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int count = Integer.parseInt(txtPerson.getText().toString());
                    if(count > 1)
                        count--;
                    txtPerson.setText(String.valueOf(count));
                }
            });

            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        }
    };

    View.OnClickListener OpenBottomSheetDialogCalendar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(m_context, R.style.BottomSheetDialogTheme);
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(m_context);

            int layout = R.layout.layout_calendar;
            LinearLayout linearLayout = v.findViewById(R.id.llCalendar);
            View bottomSheetView = LayoutInflater.from(getContext()).inflate(layout, linearLayout);

            // CalendarView calendarView = v.findViewById(R.id.searchCalendarView);
            // Calendar calendar = Calendar.getInstance();
            // calendarView.setDate(1621800274);

            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        }
    };

    View.OnClickListener OpenBottomSheetDialogDate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(m_context, R.style.BottomSheetDialogTheme);
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(m_context);

            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

            int layout = R.layout.layout_date;
            LinearLayout linearLayout = v.findViewById(R.id.llDate);
            View bottomSheetView = LayoutInflater.from(getContext()).inflate(layout, linearLayout);

            final TextView txtDateViewBegin = bottomSheetView.findViewById(R.id.txtDateViewBegin);
            txtDateViewBegin.setText(m_dateBegin);
            final TextView txtDateViewEnd = bottomSheetView.findViewById(R.id.txtDateViewEnd);
            txtDateViewEnd.setText(m_dateEnd);

            final NumberPicker dd_begin = bottomSheetView.findViewById(R.id.numberPickerDayBegin);
            dd_begin.setMinValue(1);
            dd_begin.setMaxValue(31);
            dd_begin.setValue(currentDay);

            final NumberPicker mm_begin = bottomSheetView.findViewById(R.id.numberPickerMonthBegin);
            mm_begin.setMinValue(1);
            mm_begin.setMaxValue(12);
            mm_begin.setValue(currentMonth);

            final NumberPicker yy_begin = bottomSheetView.findViewById(R.id.numberPickerYearBegin);
            yy_begin.setMinValue(currentYear);
            yy_begin.setMaxValue(2050);
            yy_begin.setValue(currentYear);

            final NumberPicker dd_end = bottomSheetView.findViewById(R.id.numberPickerDayEnd);
            dd_end.setMinValue(1);
            dd_end.setMaxValue(31);
            dd_end.setValue(currentDay + 1);

            final NumberPicker mm_end = bottomSheetView.findViewById(R.id.numberPickerMonthEnd);
            mm_end.setMinValue(1);
            mm_end.setMaxValue(12);
            mm_end.setValue(currentMonth);

            final NumberPicker yy_end = bottomSheetView.findViewById(R.id.numberPickerYearEnd);
            yy_end.setMinValue(currentYear);
            yy_end.setMaxValue(2050);
            yy_end.setValue(currentYear);

            dd_begin.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                    ByteArrayOutputStream data_begin = new ByteArrayOutputStream();
                    ByteArrayOutputStream data_end = new ByteArrayOutputStream();

                    try {
                        data_begin.write(String.valueOf(newVal).getBytes());
                        data_begin.write('.');
                        data_begin.write(String.valueOf(mm_begin.getValue()).getBytes());
                        data_begin.write('.');
                        data_begin.write(String.valueOf(yy_begin.getValue()).getBytes());

                        data_end.write(String.valueOf(dd_end.getValue()).getBytes());
                        data_end.write('.');
                        data_end.write(String.valueOf(mm_end.getValue()).getBytes());
                        data_end.write('.');
                        data_end.write(String.valueOf(yy_end.getValue()).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    txtDateViewBegin.setText(data_begin.toString());
                    txtDateViewEnd.setText(data_end.toString());
                }
            });

            mm_begin.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                    ByteArrayOutputStream data_begin = new ByteArrayOutputStream();
                    ByteArrayOutputStream data_end = new ByteArrayOutputStream();

                    try {
                        data_begin.write(String.valueOf(dd_begin.getValue()).getBytes());
                        data_begin.write('.');
                        data_begin.write(String.valueOf(newVal).getBytes());
                        data_begin.write('.');
                        data_begin.write(String.valueOf(yy_begin.getValue()).getBytes());

                        data_end.write(String.valueOf(dd_end.getValue()).getBytes());
                        data_end.write('.');
                        data_end.write(String.valueOf(mm_end.getValue()).getBytes());
                        data_end.write('.');
                        data_end.write(String.valueOf(yy_end.getValue()).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    txtDateViewBegin.setText(data_begin.toString());
                    txtDateViewEnd.setText(data_end.toString());
                }
            });

            yy_begin.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                    ByteArrayOutputStream data_begin = new ByteArrayOutputStream();
                    ByteArrayOutputStream data_end = new ByteArrayOutputStream();

                    try {
                        data_begin.write(String.valueOf(dd_begin.getValue()).getBytes());
                        data_begin.write('.');
                        data_begin.write(String.valueOf(mm_begin.getValue()).getBytes());
                        data_begin.write('.');
                        data_begin.write(String.valueOf(newVal).getBytes());

                        data_end.write(String.valueOf(dd_end.getValue()).getBytes());
                        data_end.write('.');
                        data_end.write(String.valueOf(mm_end.getValue()).getBytes());
                        data_end.write('.');
                        data_end.write(String.valueOf(yy_end.getValue()).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    txtDateViewBegin.setText(data_begin.toString());
                    txtDateViewEnd.setText(data_end.toString());
                }
            });

            dd_end.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                    ByteArrayOutputStream data_begin = new ByteArrayOutputStream();
                    ByteArrayOutputStream data_end = new ByteArrayOutputStream();

                    try {
                        data_begin.write(String.valueOf(dd_begin.getValue()).getBytes());
                        data_begin.write('.');
                        data_begin.write(String.valueOf(mm_begin.getValue()).getBytes());
                        data_begin.write('.');
                        data_begin.write(String.valueOf(yy_begin.getValue()).getBytes());

                        data_end.write(String.valueOf(newVal).getBytes());
                        data_end.write('.');
                        data_end.write(String.valueOf(mm_end.getValue()).getBytes());
                        data_end.write('.');
                        data_end.write(String.valueOf(yy_end.getValue()).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    txtDateViewBegin.setText(data_begin.toString());
                    txtDateViewEnd.setText(data_end.toString());
                }
            });

            mm_end.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                    ByteArrayOutputStream data_begin = new ByteArrayOutputStream();
                    ByteArrayOutputStream data_end = new ByteArrayOutputStream();

                    try {
                        data_begin.write(String.valueOf(dd_begin.getValue()).getBytes());
                        data_begin.write('.');
                        data_begin.write(String.valueOf(mm_begin.getValue()).getBytes());
                        data_begin.write('.');
                        data_begin.write(String.valueOf(yy_begin.getValue()).getBytes());

                        data_end.write(String.valueOf(dd_end.getValue()).getBytes());
                        data_end.write('.');
                        data_end.write(String.valueOf(newVal).getBytes());
                        data_end.write('.');
                        data_end.write(String.valueOf(yy_end.getValue()).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    txtDateViewBegin.setText(data_begin.toString());
                    txtDateViewEnd.setText(data_end.toString());
                }
            });

            yy_end.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

                    ByteArrayOutputStream data_begin = new ByteArrayOutputStream();
                    ByteArrayOutputStream data_end = new ByteArrayOutputStream();

                    try {
                        data_begin.write(String.valueOf(dd_begin.getValue()).getBytes());
                        data_begin.write('.');
                        data_begin.write(String.valueOf(mm_begin.getValue()).getBytes());
                        data_begin.write('.');
                        data_begin.write(String.valueOf(yy_begin.getValue()).getBytes());

                        data_end.write(String.valueOf(dd_end.getValue()).getBytes());
                        data_end.write('.');
                        data_end.write(String.valueOf(mm_end.getValue()).getBytes());
                        data_end.write('.');
                        data_end.write(String.valueOf(newVal).getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    txtDateViewBegin.setText(data_begin.toString());
                    txtDateViewEnd.setText(data_end.toString());
                }
            });

            bottomSheetView.findViewById(R.id.btnSaveDate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_dateBegin = txtDateViewBegin.getText().toString();
                    m_dateEnd = txtDateViewEnd.getText().toString();
                    m_btnDate.setText((m_dateBegin + " - " + m_dateEnd));
                    bottomSheetDialog.dismiss();
                }
            });

            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        }
    };

    View.OnClickListener OpenBottomSheetDialogTown = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(m_context, R.style.BottomSheetDialogTheme);
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(m_context);

            int layout = R.layout.layout_town;
            LinearLayout linearLayout = v.findViewById(R.id.llTown);
            View bottomSheetView = LayoutInflater.from(getContext()).inflate(layout, linearLayout);

            final EditText txtTown = bottomSheetView.findViewById(R.id.txtTownSearch);

            bottomSheetView.findViewById(R.id.btnSaveTown).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_btnTown.setText(txtTown.getText().toString());
                    bottomSheetDialog.dismiss();
                }
            });

            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
        }
    };
}