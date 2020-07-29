package com.example.eventplanner.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.eventplanner.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditCommentDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditCommentDialogFragment extends DialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText etComment;
    private Button btnSubmitComment;

    public EditCommentDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditCommentDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditCommentDialogFragment newInstance(String param1, String param2) {
        EditCommentDialogFragment fragment = new EditCommentDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_edit_comment_dialog, container, false);
        return getActivity().getLayoutInflater().inflate(R.layout.fragment_edit_comment_dialog, container);
    }

    @Override
    public void onResume() {
        // setting the window size of the dialog fragment
        getDialog().getWindow().setLayout(800, 700);
        // Call super onResume after sizing
        super.onResume();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        etComment = (EditText) view.findViewById(R.id.etComment);
        btnSubmitComment = view.findViewById(R.id.btnSubmitComment);
        // Fetch arguments from bundle and set title
        //String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle("Enter comment");
        // Show soft keyboard automatically and request focus to field
        etComment.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        btnSubmitComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendBackResult();
            }
        });
    }

    // Defines the listener interface
    public interface EditNameDialogListener {
        void onFinishEditDialog(String inputText);
    }

    // Call this method to send the data back to the parent fragment
    public void sendBackResult() {
        // Notice the use of `getTargetFragment` which will be set when the dialog is displayed
        EditNameDialogListener listener = (EditNameDialogListener) getTargetFragment();
        listener.onFinishEditDialog(etComment.getText().toString());
        dismiss();
    }
}