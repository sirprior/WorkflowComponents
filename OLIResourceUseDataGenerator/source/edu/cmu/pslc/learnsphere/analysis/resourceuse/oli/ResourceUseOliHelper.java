/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2013
 * All Rights Reserved
 */
package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;

import edu.cmu.pslc.datashop.dao.FileDao;
import edu.cmu.pslc.datashop.item.FileItem;
import edu.cmu.pslc.datashop.item.UserItem;
import edu.cmu.pslc.datashop.util.FileUtils;
import edu.cmu.pslc.datashop.util.LogUtils;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.DaoFactory;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.ResourceUseOliUserSessDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dao.ResourceUseOliUserSessFileDao;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item.ResourceUseOliUserSessFileItem;
import edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.item.ResourceUseOliUserSessItem;

/**
 * Helper to get data from multiple tables in the database for resource use.
 *
 * @author Hui Cheng
 * @version $Revision: 12897 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2016-02-02 00:28:49 -0500 (Tue, 02 Feb 2016) $
 * <!-- $KeyWordsOff: $ -->
 */
public class ResourceUseOliHelper {

    /** Debug logging. */
    private Logger logger = Logger.getLogger(getClass().getName());
    private static final String DEFAULT_DELIMITER = "\t";
    
   
    /** Default constructor. */
    public ResourceUseOliHelper() { }

    /**
     * ----Helping functions for FileItem
     * */    
    /**
     * Add  this file to ds_file table for the given file.
     * 
     * @param String filePath the relative file path
     * @param String fileName file name
     * @param String description description of file item
     * @return FileItem the file item to be created
     */
    public FileItem createFileItem(String baseDir, String relativeFilePath, String fileName, String description) {
        // set all related fields for FileItem
        FileItem dsFileItem = new FileItem();
        dsFileItem.setFileName(fileName);
        dsFileItem.setFilePath(relativeFilePath);
        Date now = new Date();
        dsFileItem.setAddedTime(now);
        dsFileItem.setDescription(description);
        dsFileItem.setFileType("text/plain");
        dsFileItem.setOwner(new UserItem(UserItem.DEFAULT_USER));
        File file = new File(baseDir + File.separator + relativeFilePath + File.separator + fileName);
        if (file.exists())
                dsFileItem.setFileSize(file.length());
        else
                dsFileItem.setFileSize(0L);
        FileDao fileDao = edu.cmu.pslc.datashop.dao.DaoFactory.DEFAULT.getFileDao();
        fileDao.saveOrUpdate(dsFileItem);
        return dsFileItem;
    }
    
    /**
     * Update FileItem
     * @param FileItem fileItem file item to be updated
     * */
    public void updateFileItem (FileItem fileItem) {
            FileDao fileDao = edu.cmu.pslc.datashop.dao.DaoFactory.DEFAULT.getFileDao();
            fileDao.saveOrUpdate(fileItem);
    }
    
    /**
     * Delete FileItem from database
     * @param FileItem fileItem file item to be deleted
     * */
    public void deleteFileItem (FileItem fileItem) {
            FileDao fileDao = edu.cmu.pslc.datashop.dao.DaoFactory.DEFAULT.getFileDao();
            fileDao.delete(fileItem);
    }
    
    /**
     * Get a file from ds_file table for the given id.
     * 
     * @param int fileId
     * @return FileItem for this fileId
     */
    public FileItem getFile (int fileId) {
        FileDao fileDao = edu.cmu.pslc.datashop.dao.DaoFactory.DEFAULT.getFileDao();
        return fileDao.get(fileId);
    }
    
    /**
     * ----Helping functions for physical file manipulation and read/write
     * */
    /**
     * Delete a file from file system
     * @param String baseDir base directory for documents
     * @param String relativeFilePath relative file path
     * @param String fileName file name
     * */
    public void deletePhysicalFile(String baseDir, String relativeFilePath, String fileName)
                                     throws ResourceUseOliException{
            String fileAbsPathName = baseDir + File.separator + relativeFilePath + File.separator + fileName;
            File file = new File(fileAbsPathName);
            if (!file.exists()) {
                    String errorMessage = "Error found in deletePhysicalFile(), file not found: " + fileAbsPathName;
                    logWarn(errorMessage);
                    throw ResourceUseOliException.fileNotFoundException(file);
            }
            file.delete();
    }
    
    /**
     * Copy a file, e.g. an imported file, to a designated folder of the system
     * And delete the backup file if exist
     * @param File originalFile original file to be copied
     * @param String baseDie base directory all documents should be kept
     * @param String toBeSavedFileRelativePath relative path where the new copied file should be saved
     * @param String toBeSavedFileName name of the new copied file
     * */
    public void copyImportedFile(File originalFile, String baseDir, String toBeSavedFilePath, String toBeSaveFileName) 
                    throws ResourceUseOliException {
            File toBeSavedFileDir = new File(baseDir + File.separator + toBeSavedFilePath);
            if (!toBeSavedFileDir.exists())
                    toBeSavedFileDir.mkdirs();
            File toBeSavedFile = new File(baseDir + File.separator + toBeSavedFilePath + File.separator + toBeSaveFileName);
            try {
                    FileUtils.copyFile(originalFile, toBeSavedFile);
                    //delete the back up file is exists
                    File backUpFile = new File(originalFile.getAbsoluteFile() + ".bk");
                    if (backUpFile.exists()) {
                            FileUtils.renameFile(backUpFile, originalFile);
                    }
            } catch (IOException ioex) {
                    throw ResourceUseOliException.IOExceptionFoundException(ioex);
            }
    }
    
    /**
     * Append a row to a file
     * @param String path path of the file to be written to
     * @param String row content to be appended
     * @param boolean addNewLine whether to append a new line*/
    public void writeARowToFile(String path, String row, boolean addNewLine)
                                            throws ResourceUseOliException{
            FileWriter fout = null;
            try {
                    fout = new FileWriter(path, true);
                    BufferedWriter out = new BufferedWriter(fout);
                    if (addNewLine)
                            out.write(row + "\n");
                    else 
                            out.write(row);
                    out.flush();
                    out.close();
            } catch (IOException e) {
                    String errorMessage = "IOException caughter in writeARowToFile(): " + e.getMessage();
                    logWarn(errorMessage);
                    throw ResourceUseOliException.IOExceptionFoundException(e);
            } finally {
                    try {
                            if (fout != null) {
                                fout.close();
                            }
                    } catch (IOException e) {
                            String errorMessage = "IOException caughter in writeARowToFile() close block: " + e.getMessage();
                            logWarn(errorMessage);
                            throw ResourceUseOliException.IOExceptionFoundException(e);
                    }
            }
    }
    
    /**
     * Given a File, read and return the first line.
     * @param file the File to read
     * @param field delimiter
     * @return String[] first line of the file which is hopefully the column headings
     * @throws ResourceUseOliException could occur while opening the file
     */
    public String[] getHeaderFromFile(File file, String delimiter)
                    throws ResourceUseOliException {
        String[] headers;
        try {
                Scanner sc = new Scanner(file);
                headers = sc.nextLine().split(delimiter);
                if (headers.length == 1 && headers[0].length() == 1)
                        headers = new String[0];
                sc.close();
        } catch (FileNotFoundException fEx) {
                throw ResourceUseOliException.fileNotFoundException(file);
        }
        return headers;
    }
    
    /**
     * -----Helping functions for ResourceUseOliUserSessFile and ResourceUseOliUserSess
     * */
    /**
    /**
     * Get ResourceUseOliUserSessFileItem
     * @param int resourceUseOliUserSessFileId resource use OLI user_sess file id
     * @return ResourceUseOliUserSessFileItem
     */
    public ResourceUseOliUserSessFileItem getResourceUseOliUserSessFileItem(Integer resourceUseOliUserSessFileId) {
            ResourceUseOliUserSessFileDao resourceUseOliUserSessFileDao = DaoFactory.DEFAULT.getResourceUseOliUserSessFileDao();
            return resourceUseOliUserSessFileDao.get(resourceUseOliUserSessFileId);
    }

    /**
     * Get all unique students for resource use oli user-sess file
     * @param int resourceUseId resource use id
     * @return HashMap<String, String> anon_student_id as key and real student_id as values
     */
    public List<String> getUniqueStudents(Integer resourceUseOliUserSessFileId, Integer resourceUseOliTransactionFileId) {
            ResourceUseOliUserSessDao resourceUseOliUserSessDao = DaoFactory.DEFAULT.getResourceUseOliUserSessDao();
            List<String> anonStudentIds = new ArrayList<String>();
            if (resourceUseOliUserSessFileId != null) {
                    ResourceUseOliUserSessFileItem resourceUseOliUserSessFileItem = getResourceUseOliUserSessFileItem(resourceUseOliUserSessFileId);
                    List<ResourceUseOliUserSessItem> resourceUseOliUserSessItems = resourceUseOliUserSessDao.findByResourceUseOliUserSessFile(resourceUseOliUserSessFileItem);
                    for (ResourceUseOliUserSessItem resourceUseOliUserSessItem : resourceUseOliUserSessItems) {
                            anonStudentIds.add(resourceUseOliUserSessItem.getAnonStudentId());
                    }
            } else if (resourceUseOliTransactionFileId != null) {
                    anonStudentIds = resourceUseOliUserSessDao.findAnonStudentByResourceUseOliTransactionFile(resourceUseOliTransactionFileId);
            }
            return anonStudentIds;
    }
    
    /**
     * ----Helping functions for logging
     * */
    /** Only log if debugging is enabled. 
     * @param args concatenate objects into one string */
    public void logDebug(Object... args) {
        LogUtils.logDebug(logger, args);
    }

    /** Only log if info is enabled. 
     * @param args concatenate objects into one string */
    public void logInfo(Object... args) {
        LogUtils.logInfo(logger, args);
    }
    
    /** Log warning message.
     *  @param args concatenate objects into one string */
    public void logWarn(Object... args) {
        LogUtils.logWarn(logger, args);
    }
    
    /** Log error message. 
     * @param args concatenate objects into one string */
    public void logError(Object... args) {
        LogUtils.logErr(logger, args);
    }
}
