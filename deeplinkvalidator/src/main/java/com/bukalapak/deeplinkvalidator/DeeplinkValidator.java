package com.bukalapak.deeplinkvalidator;

import com.google.gson.Gson;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class DeeplinkValidator extends JFrame {

    private JTextArea info = new JTextArea(10, 10);

    private File file;

    private DeeplinkValidator() {
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
        Gson gson = new Gson();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            DynamicDeeplink.DynamicDeeplinkV2 result = gson.fromJson(br, DynamicDeeplink.DynamicDeeplinkV2.class);

            if (result == null) throw new NullPointerException("dynamic-deeplink-v2 NullPointerException");
            if (result.deeplink == null) throw new NullPointerException("deeplink:{ NullPointerException");

            List<String> keys = collectKey(result.deeplink);
            info.append("\nKey :");
            for (String key : keys) {
                info.append("\n" + key);
            }

            info.append("\n\nVALID AS DYNAMIC DEEPLINK V2");
        } catch (Exception e) {
            info.append("\n\nINVALID AS DYNAMIC DEEPLINK V2");
            info.append("\n" + e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        parseFileV1();
    }

    private void parseFileV1() {
        Gson gson = new Gson();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            DynamicDeeplink.DynamicDeeplinkV1 result = gson.fromJson(br, DynamicDeeplink.DynamicDeeplinkV1.class);

            if (result == null)
                throw new NullPointerException("new-dynamic-deeplink NullPointerException");
            if (result.map == null)
                throw new NullPointerException("map:{ NullPointerException");
            if (result.premap == null)
                throw new NullPointerException("premap:{ NullPointerException");

            info.append("\n\nVALID AS DYNAMIC DEEPLINK V1");
        } catch (Exception e) {
            info.append("\n\nINVALID AS DYNAMIC DEEPLINK V1");
            info.append("\n" + e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private List<String> collectKey(Map<String, ?> map) {
        if (map == null) throw new NullPointerException("Map NullPointerException");
        List<String> list = new ArrayList<>(map.keySet());
        for (int i = 0; i < list.size(); i++) {
            list.set(i, list.get(i).replace("\\", "\\\\"));
        }
        return list;
    }

    private void clearText() {
        info.setText("");
        info.append("Drag and drop file here or click button Open File");
    }

    public static void main(String[] args) {
        run(new DeeplinkValidator(), 500, 300);
    }

    private static void run(JFrame frame, int width, int height) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setVisible(true);
    }

    class OpenL implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser c = new JFileChooser();
            int rVal = c.showOpenDialog(DeeplinkValidator.this);
            if (rVal == JFileChooser.APPROVE_OPTION) {
                file = c.getSelectedFile();
                parseFile();
            }
        }
    }
}