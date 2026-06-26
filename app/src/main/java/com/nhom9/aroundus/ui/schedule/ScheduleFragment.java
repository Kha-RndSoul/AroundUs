package com.nhom9.aroundus.ui.schedule;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom9.aroundus.R;
import com.nhom9.aroundus.adapter.ScheduleAdapter;
import com.nhom9.aroundus.model.Place;
import com.nhom9.aroundus.model.Schedule;
import com.nhom9.aroundus.repository.PlaceRepository;
import com.nhom9.aroundus.repository.ScheduleRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

public class ScheduleFragment extends Fragment {

    private RecyclerView rvSchedules;
    private TextView tvEmptySchedule;
    private Button btnAddSchedule;

    private ScheduleAdapter scheduleAdapter;
    private ScheduleRepository scheduleRepository;
    private PlaceRepository placeRepository;

    public ScheduleFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        rvSchedules = view.findViewById(R.id.rvSchedules);
        tvEmptySchedule = view.findViewById(R.id.tvEmptySchedule);
        btnAddSchedule = view.findViewById(R.id.btnAddSchedule);

        scheduleRepository = new ScheduleRepository();
        placeRepository = new PlaceRepository();

        scheduleAdapter = new ScheduleAdapter();
        rvSchedules.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSchedules.setAdapter(scheduleAdapter);

        scheduleAdapter.setOnScheduleActionListener(new ScheduleAdapter.OnScheduleActionListener() {
            @Override
            public void onDelete(Schedule schedule) {
                hienThiDialogXoa(schedule);
            }

            @Override
            public void onClick(Schedule schedule) {
                hienThiDialogLichTrinh(schedule);
            }
        });

        btnAddSchedule.setOnClickListener(v -> hienThiDialogLichTrinh(null));

        loadSchedules();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSchedules();
    }

    private void loadSchedules() {
        scheduleRepository.getMySchedules(schedules -> {
            scheduleAdapter.setScheduleList(schedules);

            if (schedules == null || schedules.isEmpty()) {
                tvEmptySchedule.setVisibility(View.VISIBLE);
                rvSchedules.setVisibility(View.GONE);
            } else {
                tvEmptySchedule.setVisibility(View.GONE);
                rvSchedules.setVisibility(View.VISIBLE);
            }
        });
    }

    private void hienThiDialogLichTrinh(@Nullable Schedule scheduleCanSua) {
        boolean isEditMode = scheduleCanSua != null;

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_schedule, null);

        TextView tvSelectedPlace = dialogView.findViewById(R.id.tvSelectedPlace);
        TextView tvSelectedDateTime = dialogView.findViewById(R.id.tvSelectedDateTime);
        Button btnChoosePlace = dialogView.findViewById(R.id.btnChoosePlace);
        Button btnChooseDateTime = dialogView.findViewById(R.id.btnChooseDateTime);
        EditText edtScheduleNote = dialogView.findViewById(R.id.edtScheduleNote);

        final Place[] selectedPlace = new Place[1];
        final Calendar[] selectedCalendar = new Calendar[1];

        if (isEditMode) {
            tvSelectedPlace.setText(scheduleCanSua.getPlaceName());
            tvSelectedDateTime.setText(formatDateTime(scheduleCanSua.getScheduleTimeMillis()));
            edtScheduleNote.setText(scheduleCanSua.getNote());

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(scheduleCanSua.getScheduleTimeMillis());
            selectedCalendar[0] = calendar;
        }

        btnChoosePlace.setOnClickListener(v -> {
            placeRepository.getAllPlaces(places -> {
                if (places == null || places.isEmpty()) {
                    Toast.makeText(requireContext(), "Chưa có địa điểm nào để chọn", Toast.LENGTH_SHORT).show();
                    return;
                }

                hienThiDialogChonDiaDiemCoTimKiem(places, selectedPlace, tvSelectedPlace);
            });
        });

        btnChooseDateTime.setOnClickListener(v -> {
            hienThiDateTimePicker(selectedCalendar, tvSelectedDateTime);
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(isEditMode ? "Sửa lịch trình" : "Thêm lịch trình")
                .setView(dialogView)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(isEditMode ? "Lưu thay đổi" : "Lưu", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (!isEditMode && selectedPlace[0] == null) {
                    Toast.makeText(requireContext(), "Vui lòng chọn địa điểm", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedCalendar[0] == null) {
                    Toast.makeText(requireContext(), "Vui lòng chọn ngày giờ", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedCalendar[0].getTimeInMillis() < System.currentTimeMillis()) {
                    Toast.makeText(requireContext(), "Không thể chọn thời gian trong quá khứ", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isEditMode) {
                    capNhatDuLieuScheduleCanSua(
                            scheduleCanSua,
                            selectedPlace[0],
                            selectedCalendar[0],
                            edtScheduleNote.getText().toString().trim()
                    );

                    capNhatSchedule(scheduleCanSua, dialog);
                } else {
                    Schedule schedule = taoScheduleTuDuLieuNhap(
                            selectedPlace[0],
                            selectedCalendar[0],
                            edtScheduleNote.getText().toString().trim()
                    );

                    luuSchedule(schedule, dialog);
                }
            });
        });

        dialog.show();
    }

    private void hienThiDialogChonDiaDiemCoTimKiem(
            List<Place> places,
            Place[] selectedPlace,
            TextView tvSelectedPlace
    ) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_choose_place, null);

        EditText edtSearchPlace = dialogView.findViewById(R.id.edtSearchPlace);
        ListView lvPlaces = dialogView.findViewById(R.id.lvPlaces);

        List<Place> filteredPlaces = new ArrayList<>(places);
        List<String> displayNames = new ArrayList<>();

        for (Place place : filteredPlaces) {
            displayNames.add(buildPlaceDisplayText(place));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                displayNames
        );

        lvPlaces.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Chọn địa điểm")
                .setView(dialogView)
                .setNegativeButton("Hủy", null)
                .create();

        edtSearchPlace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim().toLowerCase(Locale.ROOT);

                filteredPlaces.clear();
                displayNames.clear();

                for (Place place : places) {
                    String name = safeLower(place.getName());
                    String address = safeLower(place.getAddress());
                    String category = safeLower(place.getCategory());

                    if (name.contains(keyword)
                            || address.contains(keyword)
                            || category.contains(keyword)) {
                        filteredPlaces.add(place);
                        displayNames.add(buildPlaceDisplayText(place));
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        lvPlaces.setOnItemClickListener((parent, view, position, id) -> {
            selectedPlace[0] = filteredPlaces.get(position);
            tvSelectedPlace.setText(selectedPlace[0].getName());
            dialog.dismiss();
        });

        dialog.show();
    }

    private void hienThiDateTimePicker(
            Calendar[] selectedCalendar,
            TextView tvSelectedDateTime
    ) {
        Calendar now = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            requireContext(),
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);
                                calendar.set(Calendar.MILLISECOND, 0);

                                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                                    Toast.makeText(requireContext(), "Không thể chọn thời gian trong quá khứ", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                selectedCalendar[0] = calendar;
                                tvSelectedDateTime.setText(formatDateTime(calendar.getTimeInMillis()));
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true
                    );

                    timePickerDialog.show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private Schedule taoScheduleTuDuLieuNhap(Place place, Calendar calendar, String note) {
        Schedule schedule = new Schedule();

        schedule.setPlaceId(place.getPlaceId());
        schedule.setPlaceName(place.getName());
        schedule.setPlaceAddress(place.getAddress());
        schedule.setScheduleTimeMillis(calendar.getTimeInMillis());
        schedule.setNote(note);

        if (place.getImageUrls() != null && !place.getImageUrls().isEmpty()) {
            schedule.setPlaceImageUrl(place.getImageUrls().get(0));
        }

        return schedule;
    }

    private void luuSchedule(Schedule schedule, AlertDialog dialog) {
        scheduleRepository.addSchedule(schedule, new ScheduleRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Đã thêm lịch trình", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadSchedules();
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(requireContext(), "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hienThiDialogXoa(Schedule schedule) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa lịch trình")
                .setMessage("Bạn có chắc muốn xóa lịch trình này không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    scheduleRepository.deleteSchedule(
                            schedule.getScheduleId(),
                            new ScheduleRepository.ActionCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(requireContext(), "Đã xóa lịch trình", Toast.LENGTH_SHORT).show();
                                    loadSchedules();
                                }

                                @Override
                                public void onError(String errorMsg) {
                                    Toast.makeText(requireContext(), "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                })
                .show();
    }
    private void capNhatDuLieuScheduleCanSua(
            Schedule schedule,
            @Nullable Place selectedPlace,
            Calendar calendar,
            String note
    ) {
        if (selectedPlace != null) {
            schedule.setPlaceId(selectedPlace.getPlaceId());
            schedule.setPlaceName(selectedPlace.getName());
            schedule.setPlaceAddress(selectedPlace.getAddress());

            if (selectedPlace.getImageUrls() != null && !selectedPlace.getImageUrls().isEmpty()) {
                schedule.setPlaceImageUrl(selectedPlace.getImageUrls().get(0));
            } else {
                schedule.setPlaceImageUrl(null);
            }
        }

        schedule.setScheduleTimeMillis(calendar.getTimeInMillis());
        schedule.setNote(note);
    }

    private void capNhatSchedule(Schedule schedule, AlertDialog dialog) {
        scheduleRepository.updateSchedule(schedule, new ScheduleRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Đã cập nhật lịch trình", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                loadSchedules();
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(requireContext(), "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDateTime(long millis) {
        if (millis <= 0) {
            return "Chưa có thời gian";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    private String buildPlaceDisplayText(Place place) {
        String name = place.getName() == null ? "Không tên" : place.getName();
        String category = place.getCategory() == null ? "Khác" : place.getCategory();
        String address = place.getAddress() == null ? "" : place.getAddress();

        if (address.trim().isEmpty()) {
            return name + "\n" + category;
        }

        return name + "\n" + category + "\n" + address;
    }

    private String safeLower(String value) {
        if (value == null) {
            return "";
        }

        return value.toLowerCase(Locale.ROOT);
    }
}