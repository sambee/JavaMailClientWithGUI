package com.sam.oa.mail.client.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMultipart;
import javax.swing.table.AbstractTableModel;

// This class manages the e-mail table's data.
public class MessagesTableModel extends AbstractTableModel {
    
    // These are the names for the table's columns.
    private static final String[] columnNames = {"Sender",
    "Subject", "Date", "Attch"};
    
    // The table's list of messages.
    private ArrayList messageList = new ArrayList();
    
    // Sets the table's list of messages.
    public void setMessages(Message[] messages) {
        for (int i = messages.length - 1; i >= 0; i--) {
            messageList.add(messages[i]);
        }
        
        // Fire table data change notification to table.
        fireTableDataChanged();
    }
    
    // Get a message for the specified row.
    public Message getMessage(int row) {
        return (Message) messageList.get(row);
    }
    
    // Remove a message from the list.
    public void deleteMessage(int row) {
        messageList.remove(row);
        
        // Fire table row deletion notification to table.
        fireTableRowsDeleted(row, row);
    }
    
    public void empty(){
    	while(messageList.size()>0){
    		deleteMessage(0);
    	}
    }
    
    // Get table's column count.
    public int getColumnCount() {
        return columnNames.length;
    }
    
    // Get a column's name.
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    // Get table's row count.
    public int getRowCount() {
        return messageList.size();
    }
    
    // Get value for a specific row and column combination.
    public Object getValueAt(int row, int col) {
        try {
            Message message = (Message) messageList.get(row);
            switch (col) {
                case 0: // Sender
                    Address[] senders = message.getFrom();
                    if (senders != null || senders.length > 0) {
                        return senders[0].toString();
                    } else {
                        return "[none]";
                    }
                case 1: // Subject
                    String subject = message.getSubject();
                    if (subject != null && subject.length() > 0) {
                        return subject;
                    } else {
                        return "[none]";
                    }
                case 2: // Date
                    Date date = message.getSentDate();
                    if (date != null) {
                        return date.toString();
                    } else {
                        return "[none]";
                    }
                case 3: // Date
                	return parsemessage(message) + "";
            }
        } catch (Exception e) {
            // Fail silently.
            return "";
        }
        
        return "";
    }
    
    public static int parsemessage(Message message) throws MessagingException, IOException {
        System.out.println( "<"+message.getFrom()[0] + "> " + message.getSubject());
        Multipart multipart = (Multipart)message.getContent();
        System.out.println("     > Message has "+multipart.getCount()+" multipart elements");
        int count = 0;
          for (int j = 0; j < multipart.getCount(); j++) {
              BodyPart bodyPart = multipart.getBodyPart(j);
              if(!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                  if (bodyPart.getContent().getClass().equals(MimeMultipart.class)) {
                      MimeMultipart mimemultipart = (MimeMultipart)bodyPart.getContent();
                      System.out.println("Number of embedded multiparts "+mimemultipart.getCount());
                      for (int k=0;k<mimemultipart.getCount();k++) {
                          if (mimemultipart.getBodyPart(k).getFileName() != null) {
                              System.out.println("     > Creating file with name : "+mimemultipart.getBodyPart(k).getFileName());
                              savefile(mimemultipart.getBodyPart(k).getFileName(), mimemultipart.getBodyPart(k).getInputStream());
                          }
                      }
                  }
                continue;
              }
              System.out.println("     > Creating file with name : "+bodyPart.getFileName());
              savefile(bodyPart.getFileName(), bodyPart.getInputStream());
              
              
          }
          
          return count;
      }
    
    public static void savefile(String FileName, InputStream is) throws IOException {
    	File dir = new File("files");
    	if(!dir.exists()){
    	  dir.mkdirs();	
    	}
    	
        File f = new File("files/" + FileName);
        FileOutputStream fos = new FileOutputStream(f);
        byte[] buf = new byte[4096];
        int bytesRead;
        while((bytesRead = is.read(buf))!=-1) {
            fos.write(buf, 0, bytesRead);
        }
        fos.close();
    }
}
