package youreindeer.vk_vote;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKCommentArray;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.vote_radio)
    RadioGroup group;
    @BindView(R.id.send_button)
    Button sendButton;

    private static int PINNED_ID = 0;
    private static int OWNER_ID = -122698639;


    List<Integer> values = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        VKSdk.login(this, "group,wall");
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                VKRequest s = VKApi.wall().get(VKParameters.from(VKApiConst.OWNER_ID, "-122698639", VKApiConst.COUNT, 150));
                s.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        try {
                            JSONArray votes = response.json.getJSONObject("response").getJSONArray("items");
                            PINNED_ID = votes.getJSONObject(0).getInt("id");
                            inflateAnswer(votes);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

            }

            @Override
            public void onError(VKError error) {
                Log.d("as", "" + error.errorCode);
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void inflateAnswer(JSONArray votes) throws JSONException {
        VKRequest s = VKApi.wall().getComments(VKParameters.from
                (VKApiConst.OWNER_ID, "-122698639", VKApiConst.POST_ID,PINNED_ID,VKApiConst.COUNT, 100));
        s.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                for (int i = 0; i < ((VKCommentArray) response.parsedModel).size(); i++){
                    if ( ((VKCommentArray) response.parsedModel).get(i).from_id == Integer.parseInt(VKAccessToken.currentToken().userId)){
                        sendButton.setEnabled(false);
                        Toast.makeText(getApplicationContext(), "Уже голосовали", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        });

        for (int i = 0; i < votes.length(); i++) {

            try {
                votes.getJSONObject(i).getInt("is_pinned");
                continue;
            } catch (JSONException ignored) {

            }
            RadioButton newRadioButton = new RadioButton(this);
            newRadioButton.setText(votes.getJSONObject(i).getString("text"));
            values.add(votes.getJSONObject(i).getInt("id"));
            group.addView(newRadioButton);
        }
    }

    @OnClick(R.id.send_button)
    void check() {
        int id = values.get(group.getCheckedRadioButtonId() - 1).intValue();
        VKRequest request = VKApi.wall().addComment(VKParameters.from(VKApiConst.OWNER_ID, "-122698639", VKApiConst.POST_ID, id, "text", "+"));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKRequest request = VKApi.wall().addComment(VKParameters.from(VKApiConst.OWNER_ID, "-122698639", VKApiConst.POST_ID, PINNED_ID, "text", "+"));
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        Toast.makeText(getApplicationContext(), "Голос есть", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            }
        });
    }

}
