package com.bukalapak.deeplinkvalidator;

import com.bukalapak.lib_deeplinkvalidator.DeeplinkValidator;
import com.bukalapak.lib_deeplinkvalidator.OnDeeplinkCheckListener;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class DeeplinkValidatorUI extends JFrame {

    private JTextArea info = new JTextArea(10, 10);

    private File file;

    private DeeplinkValidatorUI() {
        clearText();
        info.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(info);
        actionDrop(scrollPane);
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(scrollPane);

        JButton open = new JButton("Open File");
        open.addActionListener(new OpenL());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(open);

        Container cp = getContentPane();
        actionDrop(cp);
        cp.add(textPanel, BorderLayout.CENTER);
        cp.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void actionDrop(Component component) {
        new FileDrop(component, new FileDrop.Listener() {
            public void filesDropped(java.io.File[] files) {
                clearText();
                if (files.length > 1) {
                    info.append("\nDrop only 1 file");
                } else {
                    try {
                        file = files[0];
                        info.append("\n" + file.getAbsolutePath()+"\n");
                        parseFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void parseFile() {
        DeeplinkValidator.check("dynamic-deeplink-v2", file.getAbsolutePath(), new OnDeeplinkCheckListener() {
            @Override
            public void onDeeplinkValid(String s) {
                info.append("\n\nVALID AS DYNAMIC DEEPLINK V2");
            }

            @Override
            public void onDeeplinkInvalid(Exception e) {
                info.append("\n\nINVALID AS DYNAMIC DEEPLINK V2");
                info.append("\n"+e.getMessage());
                parseFileV1();
            }
        });
    }

    private void parseFileV1() {
        DeeplinkValidator.check("new-dynamic-deeplink", file.getAbsolutePath(), new OnDeeplinkCheckListener() {
            @Override
            public void onDeeplinkValid(String s) {
                info.append("\n\nVALID AS DYNAMIC DEEPLINK V1");
            }

            @Override
            public void onDeeplinkInvalid(Exception e) {
                info.append("\n\nINVALID AS DYNAMIC DEEPLINK V1");
                info.append("\n"+e.getMessage());
            }
        });
    }

    private void clearText() {
        info.setText("");
        info.append("Drag and drop file here or click button Open File");
    }

    public static void main(String[] args) {
        run(new DeeplinkValidatorUI(), 500, 300);
    }

    private static void run(JFrame frame, int width, int height) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setVisible(true);
    }

    class OpenL implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser c = new JFileChooser();
            int rVal = c.showOpenDialog(DeeplinkValidatorUI.this);
            if (rVal == JFileChooser.APPROVE_OPTION) {
                file = c.getSelectedFile();
                parseFile();
            }
        }
    }
}