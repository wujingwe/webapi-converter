package me.oldjing.webapi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import me.oldjing.ui.MainActivity;

import static junit.framework.Assert.assertNotNull;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 22)
public class MainActivityTest {

	@Test
	public void shouldNotBeNull() throws Exception {
		MainActivity activity = Robolectric.setupActivity(MainActivity.class);
		assertNotNull(activity);
	}
}
