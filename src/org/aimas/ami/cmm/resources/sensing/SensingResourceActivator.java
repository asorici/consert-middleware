package org.aimas.ami.cmm.resources.sensing;

import java.util.Dictionary;
import java.util.Hashtable;

import org.aimas.ami.cmm.api.ContextAssertionAdaptor;
import org.aimas.ami.contextrep.resources.TimeService;
import org.aimas.ami.contextrep.vocabulary.ConsertCore;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


/**
 * This is the activator for the bundle holding the different ContextAssertionAdaptor implementation
 * classes for each of the ContextAssertions used in the SmartClassroom scenario.
 * Each implementation of an adaptor also specifies properties that determine which physical sensors (identified by
 * an unique URI which is present in the cmm-config.ttl file under the SensingPolicy specifications for a CtxSensor)
 * are handled by the adaptor. This is a basic mechanism which might be changed in the future.
 * 
 * @author Alex Sorici
 *
 */
public class SensingResourceActivator implements BundleActivator {
	
	
	@Override
    public void start(BundleContext context) throws Exception {
	    // Get a reference to a TimeService service
		ServiceReference<TimeService> timeServiceRef = context.getServiceReference(TimeService.class);
		TimeService timeService = context.getService(timeServiceRef);
		
		// STEP 1: Register the InformTeachingAdaptor
		Dictionary<String, Object> props = new Hashtable<String, Object>();
		String[] sensors = new String[] {
			SmartClassroom.BOOTSTRAP_NS + "TeachingActivitySensor" + " " + ConsertCore.CONTEXT_AGENT.getURI()
		};
		props.put(ContextAssertionAdaptor.ADAPTOR_IMPL_CLASS, InformTeachingAdaptor.class.getName());
		props.put(ContextAssertionAdaptor.ADAPTOR_HANDLED_SENSORS, sensors);
		
		InformTeachingAdaptor informTeachingAdaptor = new InformTeachingAdaptor();
		informTeachingAdaptor.setTimeService(timeService);
		context.registerService(ContextAssertionAdaptor.class, informTeachingAdaptor, props);
		
		// STEP 2: Register the KinectSkeletonAdaptor
		props = new Hashtable<String, Object>();
		sensors = new String[] {
			SmartClassroom.Kinect_EF210_PresenterArea.getURI() + " " + SmartClassroom.KinectCamera.getURI(),
			SmartClassroom.Kinect_EF210_Section1_Left.getURI() + " " + SmartClassroom.KinectCamera.getURI(),
			SmartClassroom.Kinect_EF210_Section1_Right.getURI() + " " + SmartClassroom.KinectCamera.getURI(),
			SmartClassroom.Kinect_EF210_Section2_Left.getURI() + " " + SmartClassroom.KinectCamera.getURI(),
			SmartClassroom.Kinect_EF210_Section2_Right.getURI() + " " + SmartClassroom.KinectCamera.getURI(),
			SmartClassroom.Kinect_EF210_Section3_Left.getURI() + " " + SmartClassroom.KinectCamera.getURI(),
			SmartClassroom.Kinect_EF210_Section3_Right.getURI() + " " + SmartClassroom.KinectCamera.getURI()
		};
		props.put(ContextAssertionAdaptor.ADAPTOR_IMPL_CLASS, KinectSkeletonAdaptor.class.getName());
		props.put(ContextAssertionAdaptor.ADAPTOR_HANDLED_SENSORS, sensors);
		
		KinectSkeletonAdaptor kinectAdaptor = new KinectSkeletonAdaptor();
		kinectAdaptor.setTimeService(timeService);
		context.registerService(ContextAssertionAdaptor.class, kinectAdaptor, props);
		
		// STEP 3: Register the NoiseLevelAdaptor
		props = new Hashtable<String, Object>();
		sensors = new String[] {
			SmartClassroom.Mic_EF210_PresenterArea.getURI() + " " + SmartClassroom.Microphone.getURI(),
			SmartClassroom.Mic_EF210_Section1_Left.getURI() + " " + SmartClassroom.Microphone.getURI(),
			SmartClassroom.Mic_EF210_Section1_Right.getURI() + " " + SmartClassroom.Microphone.getURI(),
			SmartClassroom.Mic_EF210_Section2_Left.getURI() + " " + SmartClassroom.Microphone.getURI(),
			SmartClassroom.Mic_EF210_Section2_Right.getURI() + " " + SmartClassroom.Microphone.getURI(),
			SmartClassroom.Mic_EF210_Section3_Left.getURI() + " " + SmartClassroom.Microphone.getURI(),
			SmartClassroom.Mic_EF210_Section3_Right.getURI() + " " + SmartClassroom.Microphone.getURI()
		};
		props.put(ContextAssertionAdaptor.ADAPTOR_IMPL_CLASS, NoiseLevelAdaptor.class.getName());
		props.put(ContextAssertionAdaptor.ADAPTOR_HANDLED_SENSORS, sensors);
		
		NoiseLevelAdaptor noiseAdaptor = new NoiseLevelAdaptor();
		noiseAdaptor.setTimeService(timeService);
		context.registerService(ContextAssertionAdaptor.class, noiseAdaptor, props);
		
		// STEP 4: Register the SenseBluetoothAdaptor
		props = new Hashtable<String, Object>();
		sensors = new String[] {
			SmartClassroom.PresenceSensor_EF210 + " " + SmartClassroom.PresenceSensor
		};
		props.put(ContextAssertionAdaptor.ADAPTOR_IMPL_CLASS, SenseBluetoothAdaptor.class.getName());
		props.put(ContextAssertionAdaptor.ADAPTOR_HANDLED_SENSORS, sensors);
		
		SenseBluetoothAdaptor presenceAdaptor = new SenseBluetoothAdaptor();
		presenceAdaptor.setTimeService(timeService);
		context.registerService(ContextAssertionAdaptor.class, presenceAdaptor, props);
    
		
		// STEP 5: Register the SenseLuminosityAdaptor
		props = new Hashtable<String, Object>();
		sensors = new String[] {
			SmartClassroom.Lum_EF210_PresenterArea.getURI() + " " + SmartClassroom.LuminositySensor.getURI(),
			SmartClassroom.Lum_EF210_Section1_Right.getURI() + " " + SmartClassroom.LuminositySensor.getURI(),
			SmartClassroom.Lum_EF210_Section2_Right.getURI() + " " + SmartClassroom.LuminositySensor.getURI(),
			SmartClassroom.Lum_EF210_Section3_Right.getURI() + " " + SmartClassroom.LuminositySensor.getURI()
		};
		props.put(ContextAssertionAdaptor.ADAPTOR_IMPL_CLASS, SenseLuminosityAdaptor.class.getName());
		props.put(ContextAssertionAdaptor.ADAPTOR_HANDLED_SENSORS, sensors);
		
		SenseLuminosityAdaptor luminosityAdaptor = new SenseLuminosityAdaptor();
		luminosityAdaptor.setTimeService(timeService);
		context.registerService(ContextAssertionAdaptor.class, luminosityAdaptor, props);
	
		
		// STEP 6: Register the SenseTemperatureAdaptor
		props = new Hashtable<String, Object>();
		sensors = new String[] {
			SmartClassroom.Temp_EF210_Section1_Left.getURI() + " " + SmartClassroom.TemperatureSensor.getURI(),
			SmartClassroom.Temp_EF210_Section1_Right.getURI() + " " + SmartClassroom.TemperatureSensor.getURI(),
			SmartClassroom.Temp_EF210_Section3_Left.getURI() + " " + SmartClassroom.TemperatureSensor.getURI(),
			SmartClassroom.Temp_EF210_Section3_Right.getURI() + " " + SmartClassroom.TemperatureSensor.getURI()
		};
		props.put(ContextAssertionAdaptor.ADAPTOR_IMPL_CLASS, SenseTemperatureAdaptor.class.getName());
		props.put(ContextAssertionAdaptor.ADAPTOR_HANDLED_SENSORS, sensors);
		
		SenseTemperatureAdaptor temperatureAdaptor = new SenseTemperatureAdaptor();
		temperatureAdaptor.setTimeService(timeService);
		context.registerService(ContextAssertionAdaptor.class, temperatureAdaptor, props);
	}

	@Override
    public void stop(BundleContext context) throws Exception {
	    // Nothing to do
    }
	
}
