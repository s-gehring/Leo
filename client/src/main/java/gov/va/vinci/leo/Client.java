/**
 *
 */
package gov.va.vinci.leo;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAFramework;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.resource.metadata.ProcessingResourceMetaData;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.xml.sax.SAXException;

/*
 * #%L
 * Leo
 * %%
 * Copyright (C) 2010 - 2014 Department of Veterans Affairs
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import gov.va.vinci.leo.cr.LeoCollectionReaderInterface;
import gov.va.vinci.leo.exceptions.LeoBrokerConfigurationException;
import gov.va.vinci.leo.exceptions.LeoClientInitializationException;
import gov.va.vinci.leo.exceptions.LeoClientRuntimeException;
import gov.va.vinci.leo.exceptions.LeoClientSerializationException;
import gov.va.vinci.leo.exceptions.LeoClientUserInterruptionException;
import gov.va.vinci.leo.exceptions.LeoListenerException;
import gov.va.vinci.leo.exceptions.LeoUnintializedUimaAsException;
import gov.va.vinci.leo.exceptions.LeoValidationException;
import gov.va.vinci.leo.listener.BaseListener;
import gov.va.vinci.leo.tools.LeoProperties;

/**
 * Initialize and execute a pipeline via the client API for UIMA AS which has
 * been
 * created via the Service object. Utilizes the same Engine created and provided
 * by
 * the Service.
 *
 * @author thomasginter
 */
public class Client extends LeoProperties {

    /**
     * UIMA Asynchronous Engine object used to deploy the server and later to
     * init and run the client side of the service also.
     */
    protected LeoEngine uimaAsEngine = null;

    /**
     * Application execution map used primarily by the client.
     */
    protected Map<String, Object> mAppCtx = new HashMap<>();

    /**
     * Holds performance report data from the engine after processing is complete.
     */
    protected String performanceReport = null;

    /**
     * Flag to let us know if a UAB Listener has been added.
     */
    protected boolean isUAListenerAdded = false;

    /**
     * Logger object.
     */
    protected final static Logger LOG = Logger.getLogger(Client.class);

    /**
     * Collection reader used for reading documents in and sending them to the
     * service.
     */
    protected LeoCollectionReaderInterface collectionReader = null;

    /**
     * Default Constructor, will try to load properties from file located at
     * conf/leo.properties.
     *
     * @param uaListeners
     *            Listeners that will catch Service callback events
     */
    public Client(final UimaAsBaseCallbackListener... uaListeners) {
        this.uimaAsEngine = new LeoEngine();

        this.loadDefaults();
        if (uaListeners != null) {
            for (UimaAsBaseCallbackListener uab : uaListeners) {
                this.addUimaAsBaseCallbackListener(uab);
            } // for
        } // if
    }// default constructor

    /**
     * Constructor with properties file input for client execution context.
     *
     * @param propertiesFile
     *            the full path to the properties file for client properties.
     * @param uaListeners
     *            Listeners that will catch Service callback events
     */
    public Client(final String propertiesFile, final UimaAsBaseCallbackListener... uaListeners) {
        this(uaListeners);
        this.loadprops(propertiesFile);

    }// Constructor with properties file for initialization

    /**
     * Constructor with properties file input for client execution context, a
     * collection reader for input, and option listeners.
     *
     * @param propertiesFile
     *            the full path to the properties file for client properties.
     * @param collectionReader
     *            the input collection reader for this client.
     * @param uaListeners
     *            Listeners that will catch Service callback events
     * @throws Exception
     *             if there is an error loading properties.
     */
    public Client(final String propertiesFile, final LeoCollectionReaderInterface collectionReader,
            final UimaAsBaseCallbackListener... uaListeners) throws Exception {
        this(propertiesFile, uaListeners);
        this.collectionReader = collectionReader;
    }// Constructor with properties file for initialization

    /**
     * Set the collection reader for this client.
     *
     * @param reader
     *            the collection reader for this client.
     */
    public void setLeoCollectionReader(final LeoCollectionReaderInterface reader) {
        this.collectionReader = reader;
    }

    /**
     * Get the collection reader for this client.
     *
     * @return the collection reader for this client.
     */
    public LeoCollectionReaderInterface getLeoCollectionReader() {
        return collectionReader;
    }

    /**
     * Set a reference to the UABCallbackListener object for handling service
     * events.
     *
     * @param uaListener
     *            adds a callback gov.va.vinci.leo.listener for this client.
     */
    public void addUimaAsBaseCallbackListener(final UimaAsBaseCallbackListener uaListener) {
        // Nothing to add if null given as gov.va.vinci.leo.listener
        if (uaListener == null)
            return;
        // Init the engine if not already done
        if (this.uimaAsEngine == null)
            this.uimaAsEngine = new LeoEngine();
        this.uimaAsEngine.addStatusCallbackListener(uaListener);
        this.isUAListenerAdded = true;
    }// setUABListener method

    /**
     * Validate the required data for client execution.
     *
     * @throws LeoValidationException
     *             if any data required for the client is missing.
     */
    public void validateData() throws LeoValidationException {

        if (this.uimaAsEngine == null) {
            throw new LeoUnintializedUimaAsException("The UIMA-AS engine was not initialized.");
        }

        if ((this.mAppCtx == null ||
                !this.mAppCtx.containsKey(UimaAsynchronousEngine.ServerUri)) &&
                this.mBrokerURL == null) {
            throw new LeoBrokerConfigurationException("The broker URL is undefined.");
        }

        if ((this.mAppCtx == null ||
                !this.mAppCtx.containsKey(UimaAsynchronousEngine.ENDPOINT)) &&
                this.mEndpoint == null) {
            throw new LeoBrokerConfigurationException("The queue name is undefined.");

        }

        if (!this.isUAListenerAdded) {
            throw new LeoListenerException("No UimaAsBaseCallbackListener is registered.");

        }

    }

    /**
     * Initialize the client engine and application context.
     *
     * @param baseCallbackListeners
     *            Listeners that will catch Service callback events
     * @throws LeoClientInitializationException
     *             if any data for initialization is missing
     */
    protected void initializeClient(final UimaAsBaseCallbackListener... baseCallbackListeners)
            throws LeoClientInitializationException {
        if (baseCallbackListeners != null) {
            for (UimaAsBaseCallbackListener uab : baseCallbackListeners) {
                this.addUimaAsBaseCallbackListener(uab);
            }
        }
        try {
            validateData();
        } catch (LeoValidationException e) {
            throw new LeoClientInitializationException("Unable to initialize Leo client.", e);
        }

        // Add Broker URL
        if (this.mBrokerURL != null)
            mAppCtx.put(UimaAsynchronousEngine.ServerUri, this.mBrokerURL);
        // Add endpoint
        if (this.mEndpoint != null)
            mAppCtx.put(UimaAsynchronousEngine.ENDPOINT, this.mEndpoint);
        // Add timeouts in milliseconds
        mAppCtx.put(UimaAsynchronousEngine.Timeout, mCCTimeout * 1000);
        mAppCtx.put(UimaAsynchronousEngine.GetMetaTimeout, mInitTimeout * 1000);
        mAppCtx.put(UimaAsynchronousEngine.CpcTimeout, mCCTimeout * 1000);
        // Add Cas Pool Size
        mAppCtx.put(UimaAsynchronousEngine.CasPoolSize, mCasPoolSize);
        // Add FS heap size
        mAppCtx.put(UIMAFramework.CAS_INITIAL_HEAP_SIZE,
                Integer.toString(mFSHeapSize / 4));
    }

    /**
     * Run with the collection reader that is already set in the client.
     *
     * @param uabs
     *            List of Listeners that will catch callback events from the service
     * @throws LeoValidationException
     *             if no collection reader was set
     * @throws LeoClientInitializationException
     *             if an error in initialization occurs
     */
    public void run(final UimaAsBaseCallbackListener... uabs)
            throws LeoValidationException, LeoClientInitializationException {
        if (collectionReader == null) {
            throw new LeoValidationException("Client does not have a collection reader set.");
        }
        run(this.collectionReader, uabs);
    }

    /**
     * Execute the AS pipeline using the LeoCollectionReaderInterface object.
     *
     * @param collectionReader
     *            LeoCollectionReaderInterface that will produce CASes for the
     *            pipeline
     * @param uabs
     *            List of Listeners that will catch callback events from the service
     * @throws LeoClientInitializationException
     *             if an error in initialization occurs
     */
    public void run(final LeoCollectionReaderInterface collectionReader,
            final UimaAsBaseCallbackListener... uabs) throws LeoClientInitializationException {
        this.initializeClient(uabs);
        CollectionReader reader;
        try {
            reader = collectionReader.produceCollectionReader();
            uimaAsEngine.setCollectionReader(reader);
            uimaAsEngine.initialize(mAppCtx);
        } catch (ResourceInitializationException exception) {
            throw new LeoClientInitializationException("Failed to instantiate collection reader.",
                    exception);
        }

        LOG.info("Serialization Strategy: " + uimaAsEngine.getSerialFormat());

        /**
         * Catch exceptions that are thrown during processing, output the exception to
         * the log,
         * then make sure the engine is stopped.
         */
        try {
            uimaAsEngine.process();

            // Insure all CAS have come back. (Work around a bug in UIMA 2.3.1)
            int count = 0;
            boolean finished = false;
            while (!finished && count < 10) {
                boolean listenersComplete = true;

                /** Check all listeners. **/
                for (Object listener : uimaAsEngine.getListeners()) {
                    if (listener instanceof BaseListener) {
                        BaseListener baseListener = (BaseListener) listener;
                        if (baseListener.getNumSent() != baseListener.getNumReceived()) {
                            listenersComplete = false;
                        }
                    } else {
                        LOG.warn("Listener " + listener.getClass().getCanonicalName()
                                + " does not inherit from BaseListener but is a '"
                                + listener.getClass()
                                + "', so the number sent vs. received cannot be verified.");
                    }
                }

                /** see if the are all done, or sleep for 2 seconds and try again. **/
                if (!listenersComplete) {
                    LOG.warn(
                            "Have not recieved the number of CAS back that were sent for all listeners. Sleeping for 2 seconds. ("
                                    + (count + 1) + " / 10 tries)");
                    if (count == 10) {
                        LOG.warn(
                                "Did not recieve the number of CAS back that were sent for all listeners.");
                    }
                    Thread.sleep(2000);
                } else {
                    LOG.info("All listeners have recieved all CAS they sent.");
                    finished = true;
                }
                count++;
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new LeoClientUserInterruptionException(
                    "Was interrupted while waiting for CAS results.", exception);

        } catch (ResourceProcessException exception) {
            throw new LeoClientRuntimeException("UIMA-AS failed to execute the collection reader.",
                    exception);
        } finally {

            uimaAsEngine.stop();
        }
    }

    /**
     * Run the pipeline on one document at a time using the document text provided.
     *
     * @param stream
     *            the input stream to read the document from.
     * @param uabs
     *            List of Listeners that will catch callback events from the service
     * @throws LeoClientInitializationException
     *             if inputstream could not be read from
     */
    public void run(final InputStream stream, final UimaAsBaseCallbackListener... uabs)
            throws LeoClientInitializationException {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(stream, writer);
        } catch (IOException exception) {
            throw new LeoClientInitializationException("Unable to read from input source.",
                    exception);
        }
        run(writer.toString(), uabs);
    }

    /**
     * Run the pipeline on one document at a time using the document text provided.
     *
     * @param documentText
     *            the document text to process.
     * @param uabs
     *            List of Listeners that will catch callback events from the service
     * @throws LeoClientInitializationException
     *             if an error while initialization occurs
     */
    public void run(final String documentText, final UimaAsBaseCallbackListener... uabs)
            throws LeoClientInitializationException {
        this.initializeClient(uabs);

        LOG.info("Initialize the client");
        try {
            uimaAsEngine.initialize(mAppCtx);
        } catch (ResourceInitializationException exception) {
            throw new LeoClientInitializationException("UIMA-AS failed to initialize.", exception);
        }
        LOG.info("run on document text");
        CAS cas;
        try {
            cas = uimaAsEngine.getCAS();
        } catch (ResourceInitializationException e) {
            throw new LeoClientInitializationException("UIMA-AS is not yet initialized.", e);
        } catch (Exception e) {
            throw new LeoClientInitializationException("An unexpected UIMA-AS exception happened.",
                    e);
        }
        cas.setDocumentText(documentText);
        try {
            uimaAsEngine.sendCAS(cas);

        } catch (ResourceProcessException exception) {
            throw new LeoClientRuntimeException(
                    "UIMA-AS refuses to process CAS (Hash '" + cas.hashCode() + "').",
                    exception);
        }

        try {
            uimaAsEngine.collectionProcessingComplete();
        } catch (ResourceProcessException exception) {
            throw new LeoClientRuntimeException("Failed to finalize the collection processing.",
                    exception);
        }

        this.performanceReport = uimaAsEngine.getPerformanceReport();
        uimaAsEngine.stop();
    }

    /**
     * Run the pipeline on a single CAS object.
     *
     * @param cas
     *            an individual cass to process through the client.
     * @param uabs
     *            List of Listeners that will catch callback events from the service
     * @throws Exception
     *             if any error occurs during processing
     */
    public void run(final CAS cas, final UimaAsBaseCallbackListener... uabs) throws Exception {
        this.initializeClient(uabs);
        uimaAsEngine.initialize(mAppCtx);
        uimaAsEngine.sendCAS(cas);
        uimaAsEngine.collectionProcessingComplete();
        this.performanceReport = uimaAsEngine.getPerformanceReport();
        uimaAsEngine.stop();
    }// run method with CAS object as input

    /**
     * Get the list of listeners that have been registered with the client. If no
     * listeners have been registered or the
     * engine has not been initialized, then an
     * empty list will be returned.
     *
     * @return List of registered listeners
     */
    @SuppressWarnings("rawtypes")
    public List getListeners() {
        if (uimaAsEngine != null) {
            return uimaAsEngine.getListeners();
        } else {
            return new ArrayList();
        }
    }

    /**
     * Get the performance report stored after the last "run" invocation in this
     * client object.
     *
     * @return UAEngine performance report as a String object
     */
    public String getPerformanceReport() {
        return this.performanceReport;
    }// getPerformanceReport method

    /**
     * Calls the service to determine the TypeSystemDescription it is using,
     * and returns it.
     *
     * @param brokerURL
     *            URL of the broker where the service resides.
     * @param inputQueueName
     *            Name of the endpoint, or input queue for the service to query.
     * @return the TypeSystemDescription for the remote service.
     * @throws LeoClientInitializationException
     *             If an error occurs while initializing the UIMA-AS engine or
     *             retrieving the metadata.
     */
    public static TypeSystemDescription getServiceTypeSystemDescription(final String brokerURL,
            final String inputQueueName) throws LeoClientInitializationException {
        Map<String, Object> configurationMap = new HashMap<>();
        if (StringUtils.isBlank(brokerURL) || StringUtils.isBlank(inputQueueName))
            throw new IllegalArgumentException(
                    "brokerURL and InputQueue must both be set, brokerURL: " + brokerURL
                            + ", InputQueue: " + inputQueueName);
        configurationMap.put(UimaAsynchronousEngine.ServerUri, brokerURL);
        configurationMap.put(UimaAsynchronousEngine.ENDPOINT, inputQueueName);
        UimaAsynchronousEngine engine = new BaseUIMAAsynchronousEngine_impl();
        try {
            engine.initialize(configurationMap);
        } catch (ResourceInitializationException exception) {
            throw new LeoClientInitializationException(
                    "Failed to initialialize UIMA-AS engine with given broker URL '" + brokerURL
                            + "' and input queue name '" + inputQueueName + "'.",
                    exception);
        }
        ProcessingResourceMetaData meta;
        try {
            meta = engine.getMetaData();
        } catch (ResourceInitializationException exception) {
            throw new LeoClientInitializationException("Failed to retrieve UIMA-AS metadata.",
                    exception);
        }
        return meta.getTypeSystem();
    }

    /**
     * Calls the service to determine the TypeSystemDescription it is using,
     * and returns it.
     *
     * @return the TypeSystemDescription for the remote service.
     * @throws Exception
     *             if there is an exception getting the type system from the engine.
     */
    public TypeSystemDescription getServiceTypeSystemDescription() throws Exception {
        return getServiceTypeSystemDescription(mBrokerURL, mEndpoint);
    }

    /**
     * Returns the remote service TypeSystemDescription in xml. Typically for being
     * written out to a file for other tools such as the UIMA annotation viewer to
     * use.
     *
     * @param brokerURL
     *            URL of the broker where the service resides.
     * @param inputQueueName
     *            Name of the endpoint, or input queue for the service to query.
     * @return the type system description in xml format.
     * @throws LeoClientInitializationException
     *             if any exception occurred during type system
     * @throws LeoClientSerializationException
     *             if there is any problem serializing the type system to XML.
     */
    public static String getServiceTypeSystemDescriptionXml(final String brokerURL,
            final String inputQueueName)
            throws LeoClientInitializationException, LeoClientSerializationException {
        StringWriter sw = new StringWriter();
        try {
            getServiceTypeSystemDescription(brokerURL, inputQueueName).toXML(sw);
        } catch (SAXException | IOException exception) {
            throw new LeoClientSerializationException(
                    "Failed to serialize type system description to XML.", exception);
        }
        return sw.toString();
    }

    /**
     * Returns the remote service TypeSystemDescription in xml. Typically for being
     * written out to a file for other tools such as the UIMA annotation viewer to
     * use.
     *
     * @return the type system description in xml format.
     * @throws LeoClientInitializationException
     *             if any exception occurred during type system
     * @throws LeoClientSerializationException
     *             if there is any problem serializing the type system to XML.
     */
    public String getServiceTypeSystemDescriptionXml()
            throws LeoClientInitializationException, LeoClientSerializationException {
        return getServiceTypeSystemDescriptionXml(mBrokerURL, mEndpoint);
    }

    /**
     * Extends the Base engine in order to expose the listeners in a getter.
     */
    public static class LeoEngine extends BaseUIMAAsynchronousEngine_impl {

        /**
         * Return all listeners for this engine.
         *
         * @return a list of listeners for this engine.
         */
        @SuppressWarnings("rawtypes")
        public List getListeners() {
            return this.listeners;
        }
    }

}// Client Class
