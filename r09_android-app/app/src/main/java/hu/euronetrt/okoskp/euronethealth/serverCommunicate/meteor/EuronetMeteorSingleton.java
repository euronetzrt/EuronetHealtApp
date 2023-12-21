package hu.euronetrt.okoskp.euronethealth.serverCommunicate.meteor;

import android.content.Context;

import im.delight.android.ddp.db.DataStore;

/**
 * The type Euronet meteor singleton.
 */
public class EuronetMeteorSingleton extends EuronetMeteor {

    private static EuronetMeteorSingleton mInstance;

    /**
     * Create instance euronet meteor singleton.
     *
     * @param context   the context
     * @param serverUri the server uri
     * @return the euronet meteor singleton
     */
    public synchronized static EuronetMeteorSingleton createInstance(final Context context, final String serverUri) {
        if (mInstance != null) {
            throw new IllegalStateException("An instance has already been created");
        }

        mInstance = new EuronetMeteorSingleton(context, serverUri);

        return mInstance;
    }

    /**
     * Create instance euronet meteor singleton.
     *
     * @param context   the context
     * @param serverUri the server uri
     * @param dataStore the data store
     * @return the euronet meteor singleton
     */
    public synchronized static EuronetMeteorSingleton createInstance(final Context context, final String serverUri, final DataStore dataStore) {
        if (mInstance != null) {
            throw new IllegalStateException("An instance has already been created");
        }

        mInstance = new EuronetMeteorSingleton(context, serverUri, dataStore);

        return mInstance;
    }

    /**
     * Create instance euronet meteor singleton.
     *
     * @param context         the context
     * @param serverUri       the server uri
     * @param protocolVersion the protocol version
     * @return the euronet meteor singleton
     */
    public synchronized static EuronetMeteorSingleton createInstance(final Context context, final String serverUri, final String protocolVersion) {
        if (mInstance != null) {
            throw new IllegalStateException("An instance has already been created");
        }

        mInstance = new EuronetMeteorSingleton(context, serverUri, protocolVersion);

        return mInstance;
    }

    /**
     * Create instance euronet meteor singleton.
     *
     * @param context         the context
     * @param serverUri       the server uri
     * @param protocolVersion the protocol version
     * @param dataStore       the data store
     * @return the euronet meteor singleton
     */
    public synchronized static EuronetMeteorSingleton createInstance(final Context context, final String serverUri, final String protocolVersion, final DataStore dataStore) {
        if (mInstance != null) {
            throw new IllegalStateException("An instance has already been created");
        }

        mInstance = new EuronetMeteorSingleton(context, serverUri, protocolVersion, dataStore);

        return mInstance;
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public synchronized static EuronetMeteorSingleton getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("Please call `createInstance(...)` first");
        }

        return mInstance;
    }

    /**
     * Has instance boolean.
     *
     * @return the boolean
     */
    public synchronized static boolean hasInstance() {
        return mInstance != null;
    }

    /**
     * Destroy instance.
     */
    public synchronized static void destroyInstance() {
        if (mInstance == null) {
            throw new IllegalStateException("Please call `createInstance(...)` first");
        }

        mInstance.disconnect();
        mInstance.removeCallbacks();
        mInstance = null;
    }

    private EuronetMeteorSingleton(final Context context, final String serverUri) {
        super(context, serverUri);
    }

    private EuronetMeteorSingleton(final Context context, final String serverUri, final DataStore dataStore) {
        super(context, serverUri, dataStore);
    }

    private EuronetMeteorSingleton(final Context context, final String serverUri, final String protocolVersion) {
        super(context, serverUri, protocolVersion);
    }

    private EuronetMeteorSingleton(final Context context, final String serverUri, final String protocolVersion, final DataStore dataStore) {
        super(context, serverUri, protocolVersion, dataStore);
    }

}

