import java.time.LocalDateTime;

public class UserModel {
    int id;
    String name;

    public UserModel() {
        id = -1;
        name = "";
    }

    public void setId(int id){
        this.id = id;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}