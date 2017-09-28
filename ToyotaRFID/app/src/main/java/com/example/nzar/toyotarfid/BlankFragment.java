package com.example.nzar.toyotarfid;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class BlankFragment extends Fragment {

    OnScreenSaverClosedListener onScreenSaverClosedListener;


    public BlankFragment() {
        // Required empty public constructor
    }

    public interface OnScreenSaverClosedListener {
         void onScreenSaverClosed();
    }

    public void setOnScreenSaverClosedListener(OnScreenSaverClosedListener onScreenSaverClosedListener) {
        this.onScreenSaverClosedListener = onScreenSaverClosedListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.blackfill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.remove(BlankFragment.this);
                ft.commit();
                if (onScreenSaverClosedListener != null) {
                    onScreenSaverClosedListener.onScreenSaverClosed();
                }
            }
        });
    }
}
