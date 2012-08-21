package net.muteheadlight.docksoundredir.tasker;

/*
 * Copyright 2012 two forty four a.m. LLC <http://www.twofortyfouram.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

import net.muteheadlight.docksoundredir.dockRedirCentral;
import android.os.Bundle;

/**
 * Class for managing the {@link com.twofortyfouram.locale.Intent#EXTRA_BUNDLE} for this plug-in.
 */
public final class PluginBundleManager
{
    /**
     * Private constructor prevents instantiation
     * 
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private PluginBundleManager()
    {
        throw new UnsupportedOperationException("This class is non-instantiable");
    }

    /**
     * Type: {@code String}
     * <p>
     * String message to display in a Toast message.
     */
    public static final String BUNDLE_EXTRA_NAME = "BUNDLE_EXTRA_REDIRECTION";
    
    /**
     * Method to verify the content of the bundle are correct.
     * <p>
     * This method will not mutate {@code bundle}.
     * 
     * @param bundle bundle to verify. May be null, which will always return false.
     * @return true if the Bundle is valid, false if the bundle is invalid.
     */
    public static boolean isBundleValid(final Bundle bundle)
    {
        if (null == bundle)
        {
            return false;
        }

        /*
         * Make sure the expected extras exist
         */
        if (!bundle.containsKey(BUNDLE_EXTRA_NAME))
        {
            dockRedirCentral.logD(String.format("bundle must contain extra redirection"));
            return false;
        }

        /*
         * Make sure the correct number of extras exist. Run this test after checking for specific Bundle extras above so that the
         * error message is more useful. (E.g. the caller will see what extras are missing, rather than just a message that there
         * is the wrong number).
         */
        if (1 != bundle.keySet().size())
        {
        	dockRedirCentral.logD(String.format("bundle must contain 1 key, but currently contains %d keys: %s", Integer.valueOf(bundle.keySet().size()), bundle.keySet())); //$NON-NLS-1$
            return false;
        }

        /*
         * Make sure the extra isn't null or empty
         */
        if (bundle.getInt(BUNDLE_EXTRA_NAME, -1) == -1)
        {
        	dockRedirCentral.logD(String.format("bundle extra redirect appears to be null or empty.  It must be a non-empty string")); //$NON-NLS-1$
            return false;
        }

        return true;
    }
}
