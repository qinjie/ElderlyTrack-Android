package edu.np.ece.wetrack;

import edu.np.ece.wetrack.api.ApiInterface;

public interface FragmentListener {

    public BeaconApplication getBaseApplication();

    public ApiInterface getApiInterface();

    public void setActionBarTitle(String title, boolean showBackArrow);

}

