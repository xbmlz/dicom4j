package org.dicom4j.net;

import org.dcm4che3.net.*;
import org.dcm4che3.net.service.AbstractDicomService;
import org.dcm4che3.net.service.DicomServiceRegistry;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Representation of a DICOM server.
 *
 * @author chenxc
 * @date 2020/08/05.
 */
public class DicomServer {

    /**
     * device host
     */
    private String host = "0.0.0.0";

    /**
     * device ae title
     */
    private String aeTitle = "*";

    /**
     * device port
     */
    private int port;

    /**
     * Support presentation contexts for all storage SOP Classes
     */
    private List<String> supportedContexts;

    private AbstractDicomService handler;

    /**
     * Association Handler
     */
    private AssociationHandler associationHandler;

    public DicomServer(String host, int port, String aeTitle, AbstractDicomService handler, AssociationHandler associationHandler) {
        this.host = host;
        this.aeTitle = aeTitle;
        this.port = port;
        this.handler = handler;
        this.associationHandler = associationHandler;
    }

    public DicomServer(String host, int port, String aeTitle, AbstractDicomService handler) {
        this.host = host;
        this.port = port;
        this.aeTitle = aeTitle;
        this.handler = handler;
        this.associationHandler = null;
    }

    public DicomServer(int port, AbstractDicomService handler) {
        this.port = port;
        this.handler = handler;
        this.associationHandler = null;
    }

    /**
     * start dicom service
     */
    public void start() {
        // TODO multiple service
        // register service
        DicomServiceRegistry dicomServiceRegistry = new DicomServiceRegistry();
        dicomServiceRegistry.addDicomService(handler);
        // create connection
        Connection conn = new Connection();
        conn.setHostname(host);
        conn.setPort(port);
        conn.setReceivePDULength(Connection.DEF_MAX_PDU_LENGTH);
        conn.setSendPDULength(Connection.DEF_MAX_PDU_LENGTH);
        conn.setMaxOpsInvoked(0);
        conn.setMaxOpsPerformed(0);
        // create ApplicationEntity
        ApplicationEntity ae = new ApplicationEntity(aeTitle);
        ae.addTransferCapability(
                new TransferCapability(null,
                        "*",
                        TransferCapability.Role.SCP,
                        "*"));
        ae.setAssociationAcceptor(true);
        ae.addConnection(conn);
        // create Device
        Device device = new Device(aeTitle);
        device.setDimseRQHandler(dicomServiceRegistry);
        device.addConnection(conn);
        device.addApplicationEntity(ae);
        if (null != associationHandler) {
            device.setAssociationHandler(associationHandler);
        }
        ExecutorService executorService = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        device.setScheduledExecutor(scheduledExecutorService);
        device.setExecutor(executorService);
        try {
            device.bindConnections();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }
}
