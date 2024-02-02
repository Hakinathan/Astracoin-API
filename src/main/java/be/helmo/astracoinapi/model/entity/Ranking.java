package be.helmo.astracoinapi.model.entity;

public class Ranking implements Comparable{
    private float foldersValue;
    private User user;

    public Ranking(float foldersValue, User user) {
        this.foldersValue = foldersValue;
        this.user = user;
    }

    public float getFoldersValue() {
        return foldersValue;
    }

    public void setFoldersValue(float foldersValue) {
        this.foldersValue = foldersValue;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public int compareTo(Object o) {
        Ranking temp = (Ranking) o;
        if(this.foldersValue > temp.foldersValue)
            return -1;
        else if (this.foldersValue < temp.foldersValue)
            return 1;
        else
            return 0;
    }
}
