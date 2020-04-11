package to.us.resume_builder.dbc.resume_user;

import java.io.IOException;

public class ResumeUserDBC {
    private GetRequest get;
    private PutRequest put;

    public ResumeUserDBC() {
        get = new GetRequest("/user");
        put = new PutRequest("/user");
    }

    public boolean hasUser(String name) throws IOException, InterruptedException {
        boolean flag = false;

        var v = get.sendRequest("name", name);
        if (v.statusCode() == 200) {
            System.out.println(v.body());
            flag = Boolean.getBoolean(v.body());
        }

        return flag;
    }

    public long addUser(String name) throws IOException, InterruptedException {
        long result = -1;

        var v = put.sendRequest("name", name);
        if (v.statusCode() == 200)
            result = Long.parseLong(v.body());

        return result;
    }
}
