package ankushpathak1511962.com.teamninjaskillathon;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Ankush on 29-08-2017.
 */

public class TeamNinjaSkillathon extends Application{
    @Override
    public void onCreate()
    {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }
}
