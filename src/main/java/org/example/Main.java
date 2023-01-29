package org.example;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import static org.example.Constants.*;

public class Main extends JFrame {
    private JLabel pathLabel;
    private JButton selectMergeFolderButton, selectSplitFileButton;
    private JButton mergeSplitButton;
    private String path;
    private Integer type;
    private JDialog dialog;

    public void jpegToPdf(String inputJpegPath, String outputPdfPath) {
        try {
            // Create a new PDF document
            PDDocument document = new PDDocument();

            // Create a new page and add it to the document
            PDPage page = new PDPage();
            document.addPage(page);

            // Create a PDImageXObject from the input JPEG file
            PDImageXObject image = PDImageXObject.createFromFile(inputJpegPath, document);

            // Create a content stream to write the image to the page
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Draw the image on the page
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            contentStream.drawImage(image, 0, 0, pageWidth, pageHeight);

            // Close the content stream
            contentStream.close();

            // Save the document to the output PDF file
            document.save(outputPdfPath);

            // Close the document
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void createRadioButtons () {
        ButtonGroup group = new ButtonGroup();
        JRadioButton option1 = new JRadioButton("Merge");
        option1.setBounds(RADIO_BUTTONS_START_X, RADIO_BUTTONS_START_Y, RADIO_BUTTONS_WIDTH, RADIO_BUTTONS_HEIGHT);
        option1.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                this.type = ACTION_TYPE_MERGE;
                this.selectMergeFolderButton.setVisible(true);
                this.selectSplitFileButton.setVisible(false);
            }
        });
        JRadioButton option2 = new JRadioButton("Split");
        option2.setBounds(RADIO_BUTTONS_START_X + RADIO_BUTTONS_WIDTH, RADIO_BUTTONS_START_Y, RADIO_BUTTONS_WIDTH, RADIO_BUTTONS_HEIGHT);
        option2.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                this.type = ACTION_TYPE_SPLIT;
                this.selectMergeFolderButton.setVisible(false);
                this.selectSplitFileButton.setVisible(true);
            }
        });
        group.add(option1);
        group.add(option2);
        this.add(option1);
        this.add(option2);

    }

    private void addSelectMergeFolderButton() {
        this.selectMergeFolderButton = new JButton("Select Folder With Files To Merge");
        int y = RADIO_BUTTONS_START_Y + RADIO_BUTTONS_HEIGHT;
        this.selectMergeFolderButton.setBounds((WINDOW_WIDTH - SELECT_FOLDER_BUTTON_WIDTH) / 2, y, SELECT_FOLDER_BUTTON_WIDTH, SELECT_FOLDER_BUTTON_HEIGHT);
        this.selectMergeFolderButton.addActionListener((e) -> {
            selectFolder();
        });
        this.selectMergeFolderButton.setVisible(this.type != null && this.type == ACTION_TYPE_MERGE);
        this.add(this.selectMergeFolderButton);
    }

    private void addSelectSplitFileButton() {
        this.selectSplitFileButton = new JButton("Select File To Split");
        int y = RADIO_BUTTONS_START_Y + RADIO_BUTTONS_HEIGHT;
        this.selectSplitFileButton.setBounds((WINDOW_WIDTH - SELECT_FOLDER_BUTTON_WIDTH) / 2, y, SELECT_FOLDER_BUTTON_WIDTH, SELECT_FOLDER_BUTTON_HEIGHT);
        this.selectSplitFileButton.addActionListener((e) -> {
            selectFile();
        });
        this.selectSplitFileButton.setVisible(this.type != null && this.type == ACTION_TYPE_SPLIT);
        this.add(this.selectSplitFileButton);
    }

    private void createMergeSplitButton () {
        this.mergeSplitButton = new JButton("");
        this.mergeSplitButton.setBounds((WINDOW_WIDTH - SELECT_FOLDER_BUTTON_WIDTH) / 2, this.pathLabel.getY() + this.pathLabel.getHeight(), SELECT_FOLDER_BUTTON_WIDTH, SELECT_FOLDER_BUTTON_HEIGHT);
        this.mergeSplitButton.setVisible(false);
        this.add(mergeSplitButton);
        this.mergeSplitButton.addActionListener((e) -> {
            if (this.path != null) {
                switch (this.type) {
                    case ACTION_TYPE_MERGE -> merge(this.path);
                    case ACTION_TYPE_SPLIT -> split(this.path);
                }
            }
        });
    }

    private void createInfoLabel () {
        this.pathLabel = new JLabel("");
        this.pathLabel.setBounds(RADIO_BUTTONS_START_X,
                this.selectSplitFileButton.getY() + this.selectSplitFileButton.getHeight(),
                WINDOW_WIDTH, RADIO_BUTTONS_HEIGHT);
        this.add(pathLabel);
    }


    public Main () {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.setResizable(false);
        this.setLayout(null);
        this.setLocationRelativeTo(null);
        createRadioButtons();
        addSelectMergeFolderButton();
        addSelectSplitFileButton();
        createInfoLabel();
        createMergeSplitButton();
        this.setVisible(true);
    }

    public void selectFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(CHOOSE_FILE_DEFAULT_FOLDER));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(new JFrame());
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String selectedDir = chooser.getSelectedFile().getAbsolutePath();
            File[] pdfFiles = getPdfFilesInDir(selectedDir);
            if (pdfFiles != null) {
                this.pathLabel.setText(String.format("Files path: %s (%d files)", chooser.getSelectedFile().getAbsolutePath(), pdfFiles.length));
                this.selectMergeFolderButton.setText("Select Another Folder");
                if (pdfFiles.length > 0) {
                    this.mergeSplitButton.setVisible(true);
                    this.mergeSplitButton.setText(String.format("Merge %d Files", pdfFiles.length));
                    this.path = selectedDir;
                } else {
                    this.mergeSplitButton.setVisible(false);
                }
            } else {
                this.mergeSplitButton.setVisible(false);
            }
        }
    }

    public void selectFile () {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(CHOOSE_FILE_DEFAULT_FOLDER));
        FileFilter filter = new FileNameExtensionFilter("PDF Files", "pdf", "jpg", "jpeg");
        chooser.setFileFilter(filter);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = chooser.showOpenDialog(new JFrame());
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            this.path = chooser.getSelectedFile().getAbsolutePath();
            if (!this.path.equals("")) {
                this.pathLabel.setText(String.format("File path: %s", chooser.getSelectedFile().getAbsolutePath()));
                this.selectSplitFileButton.setText("Select Another File");
                this.mergeSplitButton.setVisible(true);
                this.mergeSplitButton.setText(String.format("Split %s Into Files", this.path));
            } else {
                this.mergeSplitButton.setVisible(false);
            }

        }

    }

    private File[] getPdfFilesInDir(String dir) {
        File[] allFiles = null;
        File dirFile = new File(dir);
        if (dirFile.exists()) {
            allFiles = dirFile.listFiles();
            if (allFiles != null) {
                allFiles = Arrays.stream(allFiles)
                        .filter(file -> file.getName().endsWith(".pdf") || file.getName().endsWith(".jpeg") || file.getName().endsWith(".jpg"))
                        .toArray(File[]::new);
            }
        }
        return allFiles;
    }


    public static void main(String[] args) {
        new Main();
    }

    public void merge (String dir) {
        List<String> paths = new ArrayList<>();
        File[] allFiles = getPdfFilesInDir(dir);
        if (allFiles != null) {
            for (File file : allFiles) {
                paths.add(file.getAbsolutePath());
            }
        }
        mergePDFs(paths, dir + "\\result");
    }

    public void split (String inputFilePath) {
        PDDocument document;
        try {
            File folder = new File(inputFilePath).getParentFile();
            document = PDDocument.load(new File(inputFilePath));
            File resultFolder = new File(folder.getAbsolutePath() + String.format("\\%s_result", new File(inputFilePath).getName()));
            if (!resultFolder.exists()) {
                resultFolder.mkdir();
            }
            int pageCount = document.getNumberOfPages();
            for (int i = 0; i < pageCount; i++) {
                PDDocument pageDocument = new PDDocument();
                PDPage page = document.getPage(i);
                pageDocument.addPage(page);
                pageDocument.save(resultFolder + "\\page_" + (i+1) + ".pdf");
                pageDocument.close();
            }
            document.close();
            openFolder(resultFolder.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void mergePDFs(List<String> inputFilePaths, String outputFolder) {
        try {
            PDFMergerUtility merger = new PDFMergerUtility();
            File outputFolderObject = new File(outputFolder);
            if (!outputFolderObject.exists()) {
                if (outputFolderObject.mkdir()) {
                    System.out.println("Folder " + outputFolder + " was created");
                }
            }
            merger.setDestinationFileName(outputFolder + String.format("\\merged_%d.pdf", System.currentTimeMillis()));
            List<String> tempFiles = new ArrayList<>();
            for (String inputFilePath : inputFilePaths) {
                if (!checkFileExtension(inputFilePath, "pdf")) {
                    if (checkFileExtension(inputFilePath, "jpeg") || checkFileExtension(inputFilePath, "jpg")) {
                        String newPath = replaceFileExtension(inputFilePath, ".pdf");
                        jpegToPdf(inputFilePath, newPath);
                        merger.addSource(newPath);
                        tempFiles.add(newPath);
                    }
                } else {
                    merger.addSource(inputFilePath);
                }
            }
            merger.mergeDocuments(null);
            System.out.println("DONE!");
            for (String file : tempFiles) {
                new File(file).delete();
            }
            openFolder(outputFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openFolder(String folderToOpen) {
        JButton openFolder = new JButton("Open Folder");
        openFolder.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(new File(folderToOpen));
                dialog.dispose();
                dialog.setVisible(false);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        this.mergeSplitButton.setEnabled(false);
        JOptionPane optionPane = new JOptionPane("The folder is located at " + folderToOpen, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{openFolder}, openFolder);
        dialog = optionPane.createDialog(this, "Folder location");
        dialog.setVisible(true);
    }

    public static boolean checkFileExtension(String fileName, String extension) {
        return fileName.endsWith(extension);
    }

    public static String replaceFileExtension(String fileName, String newExtension) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            return fileName.substring(0, dotIndex) + newExtension;
        } else {
            return fileName + newExtension;
        }
    }

}