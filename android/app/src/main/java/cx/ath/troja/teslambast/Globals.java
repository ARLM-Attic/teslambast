package cx.ath.troja.teslambast;

import android.app.Application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Globals extends Application {
    public List<Vehicle> vehicles = Collections.synchronizedList(new ArrayList<Vehicle>());
}
