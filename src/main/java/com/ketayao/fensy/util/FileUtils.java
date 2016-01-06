package com.ketayao.fensy.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileUtils {
    /**
     * The number of bytes in a kilobyte.
     */
    public static final long    ONE_KB                = 1024;

    /**
     * The number of bytes in a megabyte.
     */
    public static final long    ONE_MB                = ONE_KB * ONE_KB;

    /**
     * The file copy buffer size (30 MB)
     */
    private static final long   FILE_COPY_BUFFER_SIZE = ONE_MB * 30;
    private static final String MSG_NOT_A_DIRECTORY   = "Not a directory: ";
    private static final String MSG_NOT_FOUND         = "Not found: ";
    private static final String MSG_NOT_A_FILE        = "Not a file: ";
    private static final String MSG_UNABLE_TO_DELETE  = "Unable to delete: ";

    public static void deleteDir(File dest) throws IOException {
        deleteDir(dest, true);
    }

    /**
     * Deletes a directory.
     */
    public static void deleteDir(File dest, boolean recursive) throws IOException {
        cleanDir(dest, recursive);
        if (dest.delete() == false) {
            throw new IOException(MSG_UNABLE_TO_DELETE + dest);
        }
    }

    /**
     * Cleans a directory without deleting it.
     */
    public static void cleanDir(File dest, boolean recursive) throws IOException {
        if (dest.exists() == false) {
            throw new FileNotFoundException(MSG_NOT_FOUND + dest);
        }

        if (dest.isDirectory() == false) {
            throw new IOException(MSG_NOT_A_DIRECTORY + dest);
        }

        File[] files = dest.listFiles();
        if (files == null) {
            throw new IOException("Failed to list contents of: " + dest);
        }

        for (File file : files) {
            try {
                if (file.isDirectory()) {
                    if (recursive == true) {
                        deleteDir(file, recursive);
                    }
                } else {
                    file.delete();
                }
            } catch (IOException ioex) {
                throw ioex;
            }
        }
    }

    public static Collection<File> listFiles(File root, String[] suffix, boolean recursive) {
        List<File> files = new ArrayList<File>();
        listFiles(files, root, suffix, recursive);
        return files;
    }

    static void listFiles(List<File> files, File dir, String[] suffix, boolean recursive) {
        File[] listFiles = dir.listFiles();
        for (File f : listFiles) {
            if (f.isFile()) {
                String extension = FilenameUtils.getExtension(f.getName());
                for (String e : suffix) {
                    if (StringUtils.endsWithIgnoreCase(e, extension)) {
                        files.add(f);
                        break;
                    }
                }
            } else if (f.isDirectory() && recursive) {
                listFiles(files, f, suffix, recursive);
            }
        }
    }

    /**
     * Copies a file to a new location preserving the file date.
     * <p>
     * This method copies the contents of the specified source file to the
     * specified destination file. The directory holding the destination file is
     * created if it does not exist. If the destination file exists, then this
     * method will overwrite it.
     * <p>
     * <strong>Note:</strong> This method tries to preserve the file's last
     * modified date/times using {@link File#setLastModified(long)}, however
     * it is not guaranteed that the operation will succeed.
     * If the modification operation fails, no indication is provided.
     * 
     * @param srcFile  an existing file to copy, must not be {@code null}
     * @param destFile  the new file, must not be {@code null}
     * 
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @see #copyFileToDirectory(File, File)
     */
    public static void copyFile(File srcFile, File destFile) throws IOException {
        copyFile(srcFile, destFile, true);
    }

    /**
     * Copies a file to a new location.
     * <p>
     * This method copies the contents of the specified source file
     * to the specified destination file.
     * The directory holding the destination file is created if it does not exist.
     * If the destination file exists, then this method will overwrite it.
     * <p>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
     * {@code true} tries to preserve the file's last modified
     * date/times using {@link File#setLastModified(long)}, however it is
     * not guaranteed that the operation will succeed.
     * If the modification operation fails, no indication is provided.
     *
     * @param srcFile  an existing file to copy, must not be {@code null}
     * @param destFile  the new file, must not be {@code null}
     * @param preserveFileDate  true if the file date of the copy
     *  should be the same as the original
     *
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException if source or destination is invalid
     * @throws IOException if an IO error occurs during copying
     * @see #copyFileToDirectory(File, File, boolean)
     */
    public static void copyFile(File srcFile, File destFile,
                                boolean preserveFileDate) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destFile == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (srcFile.exists() == false) {
            throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
        }
        if (srcFile.isDirectory()) {
            throw new IOException("Source '" + srcFile + "' exists but is a directory");
        }
        if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            throw new IOException(
                "Source '" + srcFile + "' and destination '" + destFile + "' are the same");
        }
        File parentFile = destFile.getParentFile();
        if (parentFile != null) {
            if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
                throw new IOException(
                    "Destination '" + parentFile + "' directory cannot be created");
            }
        }
        if (destFile.exists() && destFile.canWrite() == false) {
            throw new IOException("Destination '" + destFile + "' exists but is read-only");
        }
        doCopyFile(srcFile, destFile, preserveFileDate);
    }

    /**
     * Internal copy file method.
     * 
     * @param srcFile  the validated source file, must not be {@code null}
     * @param destFile  the validated destination file, must not be {@code null}
     * @param preserveFileDate  whether to preserve the file date
     * @throws IOException if an error occurs
     */
    private static void doCopyFile(File srcFile, File destFile,
                                   boolean preserveFileDate) throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            input = fis.getChannel();
            output = fos.getChannel();
            long size = input.size();
            long pos = 0;
            long count = 0;
            while (pos < size) {
                count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
                pos += output.transferFrom(input, pos, count);
            }
        } finally {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(fis);
        }

        if (srcFile.length() != destFile.length()) {
            throw new IOException(
                "Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
        }
        if (preserveFileDate) {
            destFile.setLastModified(srcFile.lastModified());
        }
    }

    public static void deleteFile(File dest) throws IOException {
        if (dest.exists() == false) {
            throw new FileNotFoundException(MSG_NOT_FOUND + dest);
        }
        if (dest.isFile() == false) {
            throw new IOException(MSG_NOT_A_FILE + dest);
        }
        if (dest.delete() == false) {
            throw new IOException(MSG_UNABLE_TO_DELETE + dest);
        }
    }
}
