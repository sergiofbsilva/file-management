package pt.ist.file.rest.utils.task;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import pt.ist.bennu.core.domain.User;
import pt.ist.bennu.core.domain.scheduler.WriteCustomTask;
import pt.ist.file.rest.utils.client.FMSRestClient;
import pt.ist.file.rest.utils.client.Folder;
import pt.ist.file.rest.utils.client.IDocument;
import pt.ist.file.rest.utils.domain.FileRestUtilsSystem;
import pt.ist.file.rest.utils.domain.RemoteDocument;
import pt.ist.file.rest.utils.domain.RemoteFolder;

public class DumpDirTask extends WriteCustomTask {
    private void copyFiles(final File localDir, final Folder remoteDir, final int depth) {
        final Map<String, String> map = new HashMap<String, String>();
        for (final File file : localDir.listFiles()) {
            if (file.isDirectory()) {
                final String dirName = file.getName();
                if (!dirName.contains(".svn")) {
                    out.printf("%s[D] %s\n", StringUtils.repeat("-", depth), dirName);
                    copyFiles(file, remoteDir.createFolder(dirName), depth + 1);
                }
            } else {
                out.printf("%s[F] %s\n", StringUtils.repeat("-", depth), file.getName());
                final IDocument doc = remoteDir.createDocument(file);
                map.clear();
                map.put("Caminho local", file.getAbsolutePath());
                doc.metadata(map);
            }
        }
    }

    private void copyFiles(final File localDir, final Folder remoteDir) {
        copyFiles(localDir, remoteDir, 1);
    }

    private void copyAllFiles(final Folder myRootDirectory) {
        final Folder remoteScriptsDir = myRootDirectory.createFolder("testUploadScripts");
        final File localScriptsDir = new File("/home/sfbs/workspace/svn/ksvncore/application/a3es");
        copyFiles(localScriptsDir, remoteScriptsDir);
    }

    @SuppressWarnings("static-method")
    private void uploadSingleFile(final Folder myRootDirectory) {
        final File file2upload = new File("/home/sfbs/Desktop/dmatos-full.html");
        final Folder testRest = myRootDirectory.createFolder("testREST");
        final IDocument doc = testRest.createDocument(file2upload);
        System.out.println(doc.info());
        final Map<String, String> map = new HashMap<String, String>();
        map.put("x", "1");
        map.put("y", "2");
        map.put("z", "3");
        System.out.println(doc.metadata(map));
    }

    private void testPersist(final Folder myRootDirectory) {
        final File file2upload = new File("/home/sfbs/Desktop/dmatos-full.html");
        final FileRestUtilsSystem system = FileRestUtilsSystem.getInstance();
        if (!system.hasAnyFolder()) {
            system.addFolder(myRootDirectory.createFolder("testPersist").persist());
            out.println("create testPersist");
        }
        if (system.hasAnyDocument()) {
            for (final RemoteDocument doc : system.getDocument()) {
                out.println(doc.info());
            }
        }
        final RemoteFolder remoteFolder = system.getFolder().get(0);
        final String random = UUID.randomUUID().toString();
        final Folder folder = remoteFolder.createFolder(random);
        final RemoteDocument doc = folder.createDocument(file2upload).persist();
        doc.setSystem(system);
        out.println("create dir " + random);
    }

    @Override
    protected void doService() {
        final FMSRestClient fms = new FMSRestClient("dot", User.findByUsername("ist152416").getUsername());
        final Folder rootDir = fms.getRootDirectory();
        // uploadSingleFile(rootDir);
        // copyAllFiles(rootDir);
        testPersist(rootDir);
    }

}
