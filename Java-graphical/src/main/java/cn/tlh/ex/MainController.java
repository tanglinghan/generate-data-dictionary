package cn.tlh.ex;

import cn.tlh.ex.util.MySqlUtils;
import cn.tlh.ex.util.OracleUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MainController implements Initializable {

    @FXML
    private Button testCon;

    @FXML
    private Button exportWord;

    @FXML
    private ChoiceBox<String> dbType;

    @FXML
    private TextField username;

    @FXML
    private TextField port;

    @FXML
    TextField host;

    @FXML
    private PasswordField password;

    @FXML
    private Label dirPath;

    @FXML
    private Button choisePath;

    @FXML
    private ImageView img;

    @FXML
    private ChoiceBox<String> dbName;

    @FXML
    private Label dbLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbType.setItems(FXCollections.observableArrayList("mysql", "oracle"));
        dbType.getSelectionModel().select(0);
        Image image = new Image("/head_2.jpg");
        img.setImage(image);
        dbType.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if ("mysql".equals(newValue)) {
                    dbName.setVisible(true);
                    dbLabel.setVisible(true);
                } else if ("oracle".equals(newValue)) {
                    dbName.setVisible(false);
                    dbLabel.setVisible(false);
                }
            }
        });
    }

    public void dbTouch(MouseEvent event) {
        String type = dbType.getValue();
        String user = username.getText();
        String pwd = password.getText();
        String value = dbName.getValue();
        String p = port.getText();
        String h = host.getText();
        if (value != null || "".equals(value)) {
            return;
        }
        if ("mysql".equals(type)) {
            Connection con = MySqlUtils.getConnnection(String.format("jdbc:mysql://%s:%s", h, p), user, pwd);
            if (con == null) {
                Alerts(false, "connecting to database failed");
                return;
            }
            ResultSet set = MySqlUtils.getResultSet(con, "show databases");
            try {
                List<String> list = new ArrayList<String>();
                while (set.next()) {
                    list.add(set.getString(1));
                }
                System.out.println(list.toString());
                dbName.setItems(FXCollections.observableArrayList(list));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void selectDirPath(ActionEvent event) {
        Stage mainStage = null;
        DirectoryChooser directory = new DirectoryChooser();
        directory.setTitle("选择路径");
        File file = directory.showDialog(mainStage);
        if (file != null) {
            String path = file.getPath();
            dirPath.setText(path);
        }
    }

    public void typeTouch(MouseEvent event) {
        String value = dbType.getValue();
        System.out.println(value);
        if ("mysql".equals(value)) {
            dbName.setVisible(true);
        }
        if ("oracle".equals(value)) {
            dbName.setVisible(false);
        }
    }

    public void testCon(ActionEvent event) {
        isCon();
    }

    public boolean isCon() {
        String type = dbType.getValue();
        String user = username.getText();
        String pwd = password.getText();
        String p = port.getText();
        String h = host.getText();
        if ("mysql".equals(type)) {
            Connection con = MySqlUtils.getConnnection(String.format("jdbc:mysql://%s:%s", h, p), user, pwd);
            if (con != null) {
                Alerts(true, "connected to database success");
                return true;
            } else {
                Alerts(false, "connecting to database failed");
                return false;
            }
        }
        if ("oracle".equals(type)) {
            Connection con = OracleUtils.getConnnection(String.format("jdbc:oracle:thin:@%s:%s:ORCL", h, p), user, pwd);
            if (con != null) {
                Alerts(true, "connected to database success");
                return true;
            } else {
                Alerts(false, "connecting to database failed");
                return false;
            }
        }
        return false;
    }

    public void exportDoc(ActionEvent event) {
        String type = dbType.getValue();
        String user = username.getText();
        String pwd = password.getText();
        String dir = dirPath.getText();
        String value = dbName.getValue();
        String p = port.getText();
        String h = host.getText();
        if ("未选择".equals(dir)) {
            Alerts(false, "请选择文件路径");
            return;
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("-t", type);
        map.put("-u", user);
        map.put("-p", pwd);
        map.put("-d", dir);
        map.put("-n", value);
        map.put("p", p);
        map.put("h", h);
        if ("mysql".equals(type)) {
            boolean b = check(map);
            if (!b) {
                return;
            }
            try {
                App.mySql(map);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ("oracle".equals(type)) {
            try {
                App.oracle(map);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean check(Map<String, String> map) {
        if (!map.containsKey("-n") || map.get("-n") == null || map.get("-n").equals("")) {
            Alerts(false, "请输入数据库名称！");
            return false;
        }
        if (!map.containsKey("-u") || map.get("-u") == null || map.get("-u").equals("")) {
            Alerts(false, "请输入数据库用户名！");
            return false;
        }
        if (!map.containsKey("-p") || map.get("-p") == null || map.get("-p").equals("")) {
            Alerts(false, "请输入数据库密码！");
            return false;
        }
        if (!map.containsKey("-d") || map.get("-d") == null || map.get("-d").equals("")) {
            Alerts(false, "请输入保存文件的目录！");
            return false;
        }
        return true;
    }


    public static void Alerts(boolean is, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        if (is) {
            alert.setTitle("Dialog");
            alert.setHeaderText(null);
            alert.setContentText(content);
        } else {
            alert.setTitle("Dialog");
            alert.setHeaderText(null);
            alert.setContentText(content);
        }
        alert.showAndWait();
    }

}