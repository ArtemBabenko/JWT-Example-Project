package test.tuto_passport.entities;

//клас по всей видимости модель для пост запроса
// которая содержит в себе переменный для етого запроса,
// а так же гетеры и сетеры для каждого елемента запроса
public class Post {

    int id;
    String title;
    String body;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
