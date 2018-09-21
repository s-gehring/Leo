package gov.va.vinci.leo;

import static gov.va.vinci.leo.SampleService.simpleServiceDefinition;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.uima.UIMAFramework;
import org.apache.uima.aae.AsynchAECasManager_impl;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.cas.CAS;
import org.apache.uima.resource.Resource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.resource.ResourceProcessException;

/*
 * #%L
 * Leo Client
 * %%
 * Copyright (C) 2010 - 2017 Department of Veterans Affairs
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
import gov.va.vinci.leo.descriptors.LeoAEDescriptor;
import gov.va.vinci.leo.exceptions.LeoClientInitializationException;
import gov.va.vinci.leo.exceptions.LeoValidationException;
import gov.va.vinci.leo.listener.MockUimaASProcessStatus;

/**
 * Created by thomasginter on 3/30/16.
 */
public class MockClient extends Client {

    /**
     * Default Constructor, will try to load properties from file located at
     * conf/leo.properties.
     *
     * @param uaListeners
     *            Listeners that will catch Service callback events
     */
    public MockClient(final UimaAsBaseCallbackListener... uaListeners) {
        this.uimaAsEngine = new LeoEngine();

        this.loadDefaults();
        if (uaListeners != null) {
            for (UimaAsBaseCallbackListener uab : uaListeners) {
                this.addUimaAsBaseCallbackListener(uab);
            } // for
        } // if
    }

    /**
     * Constructor with properties file input for client execution context.
     *
     * @param propertiesFile
     *            the full path to the properties file for client properties.
     * @param uaListeners
     *            Listeners that will catch Service callback events
     * @throws Exception
     *             if there is an error loading properties.
     */
    public MockClient(final String propertiesFile,
            final UimaAsBaseCallbackListener... uaListeners) {
        super(propertiesFile, uaListeners);
    }

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
    public MockClient(final String propertiesFile,
            final LeoCollectionReaderInterface collectionReader,
            final UimaAsBaseCallbackListener... uaListeners) {
        super(propertiesFile, collectionReader, uaListeners);
    }

    public LeoEngine getEngine() {
        return (LeoEngine) this.uimaAsEngine;
    }

    /**
     * Run with the collection reader that is already set in the client.
     *
     * @param uabs
     *            List of Listeners that will catch callback events from the service
     */
    @Override
    public void run(final UimaAsBaseCallbackListener... uabs)
            throws LeoValidationException, LeoClientInitializationException {
        super.run(uabs);

    }

    /**
     * Execute the AS pipeline using the LeoCollectionReaderInterface object.
     *
     * @param collectionReader
     *            LeoCollectionReaderInterface that will produce CASes for the
     *            pipeline
     * @param uabs
     *            List of Listeners that will catch callback events from the service
     *
     */
    @Override
    public void run(final LeoCollectionReaderInterface collectionReader,
            final UimaAsBaseCallbackListener... uabs) throws LeoClientInitializationException {
        super.run(collectionReader, uabs);
    }

    /**
     * Run the pipeline on one document at a time using the document text provided.
     *
     * @param stream
     *            the input stream to read the document from.
     * @param uabs
     *            List of Listeners that will catch callback events from the service
     *
     */
    @Override
    public void run(final InputStream stream, final UimaAsBaseCallbackListener... uabs)
            throws LeoClientInitializationException {
        super.run(stream, uabs);
    }

    /**
     * Run the pipeline on one document at a time using the document text provided.
     *
     * @param documentText
     *            the document text to process.
     * @param uabs
     *            List of Listeners that will catch callback events from the service
     *
     */
    @Override
    public void run(final String documentText, final UimaAsBaseCallbackListener... uabs)
            throws LeoClientInitializationException {
        super.run(documentText, uabs);
    }

    /**
     * Run the pipeline on a single CAS object.
     *
     * @param cas
     *            an individual cass to process through the client.
     * @param uabs
     *            List of Listeners that will catch callback events from the service
     *
     *
     */
    @Override
    public void run(final CAS cas, final UimaAsBaseCallbackListener... uabs) throws Exception {
        super.run(cas, uabs);
    }

    // Mock Engine methods allowing us to run as though there were a service up and
    // running
    public static class LeoEngine extends Client.LeoEngine {

        AnalysisEngine ae = null;

        public LeoEngine() {
            try {
                ae = UIMAFramework.produceAnalysisEngine(
                        simpleServiceDefinition().getAnalysisEngineDescription());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void setAnalysisEngineFromDescription(final LeoAEDescriptor descriptor) {
            if (descriptor == null) {
                throw new RuntimeException("Cannot create AnalysisEngine from null descriptor!");
            }
            try {
                ae = UIMAFramework.produceAnalysisEngine(descriptor.getAnalysisEngineDescription());
            } catch (ResourceInitializationException e) {
                throw new RuntimeException(e);
            }
        }

        public AnalysisEngine getAnalysisEngine() {
            return ae;
        }

        @Override
        public synchronized void initialize(final Map anApplicationContext)
                throws ResourceInitializationException {
            if (!anApplicationContext.containsKey(UimaAsynchronousEngine.ServerUri)) {
                throw new ResourceInitializationException();
            }
            if (!anApplicationContext.containsKey(UimaAsynchronousEngine.ENDPOINT)) {
                throw new ResourceInitializationException();
            }
            ResourceManager rm = null;
            if (anApplicationContext.containsKey(Resource.PARAM_RESOURCE_MANAGER)) {
                rm = (ResourceManager) anApplicationContext.get(Resource.PARAM_RESOURCE_MANAGER);
            } else {
                rm = UIMAFramework.newDefaultResourceManager();
            }
            asynchManager = new AsynchAECasManager_impl(rm);

            brokerURI = (String) anApplicationContext.get(UimaAsynchronousEngine.ServerUri);
            List listeners = this.getListeners();
            for (int i = 0; listeners != null && i < listeners.size(); i++) {
                UimaAsBaseCallbackListener listener = (UimaAsBaseCallbackListener) listeners.get(i);
                listener.initializationComplete(null);
            }
            initialized = true;
            remoteService = true;
            running = true;
            state = ClientState.RUNNING;
        }

        @Override
        public synchronized void process() throws ResourceProcessException {
            try {
                List listeners = this.getListeners();
                while (collectionReader.hasNext()) {
                    CAS cas = ae.newCAS();
                    collectionReader.getNext(cas);
                    sendCAS(cas);
                }
                this.collectionProcessingComplete();
            } catch (Exception e) {
                throw new ResourceProcessException(e);
            }
        }

        @Override
        public synchronized String sendCAS(final CAS aCAS) throws ResourceProcessException {
            List listeners = this.getListeners();
            ae.process(aCAS);
            for (int i = 0; listeners != null && i < listeners.size(); i++) {
                UimaAsBaseCallbackListener listener = (UimaAsBaseCallbackListener) listeners.get(i);
                listener.onBeforeMessageSend(new MockUimaASProcessStatus());
                listener.entityProcessComplete(aCAS, null);
            }
            return UUID.randomUUID().toString();
        }

        @Override
        public CAS getCAS() throws Exception {
            return ae.newCAS();
        }

        @Override
        public synchronized void collectionProcessingComplete() throws ResourceProcessException {
            List listeners = this.getListeners();
            for (int i = 0; listeners != null && i < listeners.size(); i++) {
                UimaAsBaseCallbackListener listener = (UimaAsBaseCallbackListener) listeners.get(i);
                listener.collectionProcessComplete(null);
            }
        }

        @Override
        public void stop() {
            running = false;
        }
    }
}
