package org.motechproject.ghana.national.tools.seed.data;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.motechproject.cmslite.api.model.StreamContent;
import org.motechproject.cmslite.api.service.CMSLiteService;
import org.motechproject.ghana.national.domain.mobilemidwife.Language;
import org.motechproject.ghana.national.tools.seed.Seed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

@Component("ivrContentSeed")
public class IVRContentSeed extends Seed {

    @Autowired
    CMSLiteService cmsLiteService;

     Logger logger = LoggerFactory.getLogger(IVRContentSeed.class);

    @Override
    protected void load() {
        FileInputStream audioInputStream = null;
        CheckedInputStream checkedInputStream = null;
        try {
            Collection<File> files = FileUtils.listFiles(new File("ghana-national-tools/src/main/resources/test-data"), new String[]{"wav"}, false);
            for (File file : files) {
                audioInputStream = new FileInputStream(file);
                checkedInputStream = new CheckedInputStream(audioInputStream, new Adler32());

                cmsLiteService.addContent(new StreamContent(Language.EN.getValue(), file.getName(),
                        audioInputStream, String.valueOf(checkedInputStream.getChecksum().getValue()), "audio/x-wav;charset=UTF-8"));
            }

        } catch (Exception e) {
           logger.error("Exception occurred while uploading audio files to cms", e);
        } finally {
            IOUtils.closeQuietly(audioInputStream);
            IOUtils.closeQuietly(checkedInputStream);
        }
    }
}