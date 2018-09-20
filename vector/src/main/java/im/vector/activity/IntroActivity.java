package im.vector.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;

import im.vector.R;
import im.vector.util.PreferencesManager;

public class IntroActivity extends AppIntro {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SliderPage sliderPageOne = new SliderPage();
        sliderPageOne.setTitle(getString(R.string.slide_one_title));
        sliderPageOne.setDescription(getString(R.string.slide_one_content));
        sliderPageOne.setImageDrawable(R.drawable.slide_one_big);
        sliderPageOne.setBgColor(getResources().getColor(R.color.grey825));
        addSlide(AppIntroFragment.newInstance(sliderPageOne));

        SliderPage sliderPageTwo = new SliderPage();
        sliderPageTwo.setTitle(getString(R.string.slide_two_title));
        sliderPageTwo.setDescription(getString(R.string.slide_two_content));
        sliderPageTwo.setImageDrawable(R.drawable.slide_two_big);
        sliderPageTwo.setBgColor(getResources().getColor(R.color.grey825));
        addSlide(AppIntroFragment.newInstance(sliderPageTwo));

        SliderPage sliderPageThree = new SliderPage();
        sliderPageThree.setTitle(getString(R.string.slide_three_title));
        sliderPageThree.setDescription(getString(R.string.slide_three_content));
        sliderPageThree.setImageDrawable(R.drawable.slide_three_big);
        sliderPageThree.setBgColor(getResources().getColor(R.color.grey825));
        addSlide(AppIntroFragment.newInstance(sliderPageThree));

        SliderPage sliderPageFour = new SliderPage();
        sliderPageFour.setTitle(getString(R.string.slide_four_title));
        sliderPageFour.setDescription(getString(R.string.slide_four_content));
        sliderPageFour.setImageDrawable(R.drawable.slide_four_big);
        sliderPageFour.setBgColor(getResources().getColor(R.color.grey825));
        addSlide(AppIntroFragment.newInstance(sliderPageFour));

        setProgressButtonEnabled(true);
        showSkipButton(false);
        showStatusBar(false);
        setGoBackLock(true);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        PreferencesManager.setIntroShown(this);
        finish();
    }
}