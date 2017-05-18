package com.dwak.lastcall;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.privacystreams.commons.list.ListOperators;
import com.github.privacystreams.communication.Call;
import com.github.privacystreams.communication.Contact;
import com.github.privacystreams.core.UQI;
import com.github.privacystreams.core.exceptions.PSException;
import com.github.privacystreams.core.purposes.Purpose;

import java.util.List;

/**
 * Created by vishnu on 12/29/13.
 */
public class LastCallDirectDialActivity extends Activity {
    private static final String TAG = LastCallDirectDialActivity.class.getSimpleName();
    private List<Object> recentCalledNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        Button callButton = (Button) findViewById(R.id.button);
        EditText phoneNumberEditText = (EditText) findViewById(R.id.editText);
        TextView nameTextView = (TextView) findViewById(R.id.textView);
        recentCalledNumbers = null;
        UQI uqi = new UQI(LastCallDirectDialActivity.this);
        try {
            recentCalledNumbers =
                    uqi.getData(Call.getLogs(), Purpose.SOCIAL("finding your recent called contacts."))
                            .filter("type", "outgoing")
                            .sortBy("timestamp")
                            .reverse()
                            .limit(1)
                            .asList("contact");
        } catch (PSException e) {
            Log.e(TAG, e.getMessage());

            if (e.isPermissionDenied()) {
                Log.e(TAG, e.getMessage());
            }
        }
        if (!recentCalledNumbers.isEmpty()) {
            phoneNumberEditText.setText((CharSequence) recentCalledNumbers.get(0));
            try {
                List<String> nameList = uqi.getData(Contact.getAll(), Purpose.SOCIAL("get recently called number"))
                        .filter(ListOperators.intersects("phones", recentCalledNumbers.toArray()))
                        .asList("name");
                if (!nameList.isEmpty()) {
                    nameTextView.setText(nameList.get(0));
                } else {
                    nameTextView.setVisibility(View.INVISIBLE);
                }
            } catch (PSException e) {
                e.printStackTrace();
            }
        }
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent dialIntent = new Intent(Intent.ACTION_CALL);
                if (!recentCalledNumbers.isEmpty()) {
                    Log.e(TAG, "onUpdateData: " + recentCalledNumbers.get(0));
                    dialIntent.setData(Uri.parse("tel:" + recentCalledNumbers.get(0)));
                    startActivity(dialIntent);
                }
            }
        });
    }
}
