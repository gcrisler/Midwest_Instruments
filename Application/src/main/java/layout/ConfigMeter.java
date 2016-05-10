package layout;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.midwestinstruments.watermeter.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConfigMeter.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConfigMeter#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigMeter extends DialogFragment {

	public ConfigMeter() {
		// Required empty public constructor
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_config_meter, container, false);
		v.findViewById(R.id.list);

		return super.onCreateView(inflater, container, savedInstanceState);
	}
}
