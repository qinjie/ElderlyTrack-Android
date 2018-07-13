package edu.np.ece.elderlytrack;

import edu.np.ece.elderlytrack.api.ApiInterface;

public interface FragmentListener {

    public BeaconApplication getBaseApplication();

    public ApiInterface getApiInterface();

    public void setActionBarTitle(String title, boolean showBackArrow);

}

