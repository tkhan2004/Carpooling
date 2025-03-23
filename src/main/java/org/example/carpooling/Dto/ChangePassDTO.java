package org.example.carpooling.Dto;

public class ChangePassDTO {
    private  String oldPass;
    private String newPass;

    public String getOldPass() {
        return oldPass;
    }

    public void setOldPass(String oldPass) {
        this.oldPass = oldPass;
    }

    public String getNewPass() {
        return newPass;
    }

    public void setNewPass(String newPass) {
        this.newPass = newPass;
    }

    public ChangePassDTO(String oldPass, String newPass) {
        this.oldPass = oldPass;
        this.newPass = newPass;
    }
}
