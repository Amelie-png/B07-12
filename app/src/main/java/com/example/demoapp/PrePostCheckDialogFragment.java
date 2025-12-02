package com.example.demoapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment that displays a pre/post-check dialog-style layout.
 *
 * <p>This fragment is generated using the default Android Studio
 * fragment template, which includes optional initialization parameters
 * (param1, param2). These parameters can be used later if the dialog
 * requires dynamic text or configuration.</p>
 *
 * <p>Currently, the fragment simply inflates the associated layout
 * and does not perform any additional logic.</p>
 */
public class PrePostCheckDialogFragment extends Fragment {

    /** Key for argument 1 passed into newInstance(). */
    private static final String ARG_PARAM1 = "param1";

    /** Key for argument 2 passed into newInstance(). */
    private static final String ARG_PARAM2 = "param2";

    /** Optional parameter 1 used to configure the fragment. */
    private String mParam1;

    /** Optional parameter 2 used to configure the fragment. */
    private String mParam2;

    /**
     * Required empty public constructor.
     * <p>Fragments must always have a no-argument constructor
     * so the system can recreate them when needed.</p>
     */
    public PrePostCheckDialogFragment() { }

    /**
     * Factory method used to create a new instance of this fragment
     * and pass initialization parameters safely.
     *
     * <p>This is the recommended pattern for fragments because
     * arguments are preserved across configuration changes (rotation, process recreation).</p>
     *
     * @param param1 Optional string parameter for customization.
     * @param param2 Optional string parameter for customization.
     * @return A configured instance of {@link PrePostCheckDialogFragment}.
     */
    public static PrePostCheckDialogFragment newInstance(String param1, String param2) {
        PrePostCheckDialogFragment fragment = new PrePostCheckDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when the fragment is first created.
     *
     * <p>This method retrieves the arguments passed into newInstance()
     * and assigns them to member variables for later use.</p>
     *
     * @param savedInstanceState Saved state (unused in this fragment)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * Called to inflate the layout associated with this fragment.
     *
     * <p>No additional setup is performed here, but UI elements may
     * be connected later if required.</p>
     *
     * @param inflater Layout inflater used to inflate XML
     * @param container Parent container the fragment lives inside
     * @param savedInstanceState Saved state (unused)
     * @return The inflated root view for this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.fragment_pre_post_check_dialog,
                container,
                false
        );
    }
}
