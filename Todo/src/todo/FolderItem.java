/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package todo;

/**
 *
 * @author aled
 */
public class FolderItem {

    private int folderId;
    private String folderName;

    public FolderItem(int folderId, String folderName) {
        this.folderId = folderId;
        this.folderName = folderName;
    }

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
    
    

}
