package ankushpathak1511962.com.teamninjaskillathon;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.DropBoxManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Form extends AppCompatActivity {
    EditText editTextSerialNo, editTextFirstName, editTextPhoneNo, editTextEmail, editTextLastName;
    TextView textViewSerialNo;
    Spinner spinnerCountryCode;
    Calendar calendar;
    Button buttonSubmit;
    int currentSerial = 1;
    DatabaseReference databaseReference, databaseReferenceCounter, databaseReferenceDay;

    //Set respective boolean to false here when adding validation code
    boolean submitName = true, submitPhone = true, submitEmail = true;

    String serial = "";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        initialise();

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean phoneVerify=verifyPhone();
                if(submitEmail && submitName && submitPhone) {
                    Entry entry = new Entry();
                    entry.setName(editTextFirstName.getText().toString() + editTextLastName.getText().toString());
                    entry.setEmail(editTextEmail.getText().toString());
                    entry.setPhoneNo(editTextPhoneNo.getText().toString());
                    entry.setSerialNo(serial);

                    databaseReference.push().setValue(entry);
                    databaseReferenceCounter.setValue(currentSerial+1);

                    Toast.makeText(Form.this,"Done",Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(Form.this,MainActivity.class));
                    finish();
                }
                if(!submitName)
                {
                    Toast.makeText(Form.this,"Error in field name",Toast.LENGTH_LONG).show();
                }
                if(!submitEmail)
                {
                    Toast.makeText(Form.this,"Error in field email",Toast.LENGTH_LONG).show();
                }
                if(!submitPhone)
                {
                    Toast.makeText(Form.this,"Error in field phone",Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    void initialise()
    {
        editTextEmail = (EditText)findViewById(R.id.editTextEmail);
        editTextFirstName = (EditText)findViewById(R.id.editTextFirstName);
        editTextLastName = (EditText)findViewById(R.id.editTextLastName);
        editTextPhoneNo = (EditText)findViewById(R.id.editTextPhoneNo);
        editTextSerialNo = (EditText)findViewById(R.id.editTextSerialNo);
        textViewSerialNo = (TextView)findViewById(R.id.textViewSerialNo);
        buttonSubmit = (Button)findViewById(R.id.buttonSubmit);
        //TODO Populate spinner
        spinnerCountryCode = (Spinner)findViewById(R.id.spinnerCountryCode);
        populateSpinner();



        progressDialog = new ProgressDialog(Form.this);
        progressDialog.setTitle("Generating serial no");
        progressDialog.setMessage("Please wait");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();

        //To ensure no one can submit before serial no is generated
        editTextLastName.setEnabled(false);
        editTextSerialNo.setEnabled(false);
        editTextPhoneNo.setEnabled(false);
        editTextEmail.setEnabled(false);
        editTextFirstName.setEnabled(false);
        buttonSubmit.setEnabled(false);

        //Firebase init
        databaseReference = FirebaseDatabase.getInstance().getReference().child("TeamNinja");
        databaseReferenceCounter = FirebaseDatabase.getInstance().getReference().child("counter");
        databaseReferenceDay = FirebaseDatabase.getInstance().getReference().child("day");
        databaseReference.keepSynced(true);
        databaseReferenceCounter.keepSynced(true);
        databaseReferenceDay.keepSynced(true);

        //Generating serial no and putting it on edit text and text view
        databaseReferenceDay.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int dayFromFB = dataSnapshot.getValue(Integer.class);
                calendar = Calendar.getInstance();
                if((int)calendar.get(Calendar.DAY_OF_MONTH) != dayFromFB)
                {
                    databaseReferenceCounter.setValue(1);
                    databaseReferenceDay.setValue(calendar.get(Calendar.DAY_OF_MONTH));

                    //Add serial no to edit text and text view and enable views
                    setup();
                }
                else
                {
                    databaseReferenceCounter.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            currentSerial = dataSnapshot.getValue(Integer.class);
                            //Add serial no to edit text and text view and enable views
                            setup();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    void setup()
    {
        editTextLastName.setEnabled(true);
        editTextPhoneNo.setEnabled(true);
        editTextEmail.setEnabled(true);
        editTextFirstName.setEnabled(true);
        buttonSubmit.setEnabled(true);
        calendar = Calendar.getInstance();
        serial += calendar.get(Calendar.YEAR);
        serial += String.format("%02d",calendar.get(Calendar.MONTH));
        serial += String.format("%02d",calendar.get(Calendar.DAY_OF_MONTH));
        serial += String.format("%02d",currentSerial);
        editTextSerialNo.setText(serial);
        textViewSerialNo.setText("Serial no : " + serial);
        progressDialog.dismiss();
    }

    void populateSpinner()
    {
        List<String> countryCode=new ArrayList<String>();
        String strTens,strUnits,strFinal;
        //below code generates 00 to 99 and adds to List countryCode.
        for(int i=0;i<10;i++)
        {
            strTens=Integer.toString(i);
            for(int j=0;j<10;j++)
            {
                strUnits=Integer.toString(j);
                strFinal=strTens.concat(strUnits);
                countryCode.add(strFinal);

            }
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,countryCode);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountryCode.setAdapter(adapter);


    }

    boolean verifyPhone()
    {
        String phoneNo=editTextPhoneNo.toString();
        phoneNo.trim(); //remove spaces from both ends of string
        if(phoneNo.length()!=10)
        {
            return false;
        }

        String[] invalidNo={"1111111111","2222222222","3333333333","4444444444","5555555555","6666666666","7777777777","8888888888","9999999999"};
        for(int i=0;i<9;i++)
        {
            if(phoneNo.equals(invalidNo[i]))
            {
                return false;
            }
        }
        return true;

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Form.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
