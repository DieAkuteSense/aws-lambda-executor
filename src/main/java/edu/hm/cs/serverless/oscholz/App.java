package edu.hm.cs.serverless.oscholz;

import com.amazonaws.regions.Regions;

import java.util.HashMap;
import java.util.Map;

/**
 * Application to call, test and evaluate regarding AWS Lambda Instance
 *
 * @author Oliver Scholz
 */
public class App {

	private static final Map<String, Integer> KNOWN_VMS = new HashMap<>();
	private static final Map<String, Integer> KNOWN_INSTANCES = new HashMap<>();

	/**
	 * Start a execution.
	 *
	 * @param args CLI arguments, must contain Lambda-ARN and one of "-lt", "-rt", "-du", "-co"
	 * @throws InterruptedException May occur in Thread
	 */
	public static void main(String[] args) throws InterruptedException {

		// ARN, AWS Region of AWS Lambda and Evaluator-Type will be read from CLI,
		// so args must have three values (or four, if "-co")
		if (args.length < 3) {
			return;
		}

		String lambdaArn = args[0];

		Regions awsRegion = Regions.fromName(args[1]);

		System.out.println("Running AWS Lambda:\nARN: " + lambdaArn + "\nRegion: " + awsRegion.getName());

		if ("-lt".equalsIgnoreCase(args[2])) {
			if (args[3] == null) {
				System.out.println("Missing CLI argument!");
				return;
			}
			System.out.println("Running LifetimeEvaluator (polling interval: " + args[3] + ").");
			LifetimeEvaluator.run(lambdaArn, awsRegion, Integer.parseInt(args[3]));
		}

		if ("-rt".equalsIgnoreCase(args[2])) {
			System.out.println("Running RuntimeEvaluator.");
			RuntimeEvaluator.determine(lambdaArn, awsRegion);
		}

		if ("-du".equalsIgnoreCase(args[2])) {
			System.out.println("Running DurableExecutor.");
			Runnable r = new DurableExecutor(lambdaArn, awsRegion);
			startAndPrintThread(r);
		}

		if ("-co".equalsIgnoreCase(args[2])) {
			if (args[3] == null) {
				System.out.println("Missing CLI argument!");
				return;
			}
			System.out.println("Running CountedExecutor (" + args[3] + " runs).");
			Runnable r = new CountedExecutor(lambdaArn, awsRegion, Integer.parseInt(args[2]));
			startAndPrintThread(r);
		}
	}

	/**
	 * Start a thread with the given runnable and print information about visited Lambda VMs and Instances.
	 *
	 * @param r Runnable to execute, {@link CountedExecutor} or {@link DurableExecutor}
	 * @throws InterruptedException Thread could throw this...
	 */
	private static void startAndPrintThread(final Runnable r) throws InterruptedException {
		Thread t = new Thread(r);
		t.start();
		t.join();

		System.out.println("--- VMs: " + KNOWN_VMS.size());
		KNOWN_VMS.forEach((k, v) -> System.out.println(String.format("%s: %d", k, v)));
		System.out.println("--- Instances: " + KNOWN_INSTANCES.size());
	}

	/**
	 * Increase counter of visited instance. If instance is unknown, add to map with count 1.
	 *
	 * @param instanceId ID of the visited Lambda instance.
	 */
	public synchronized static void reportInstance(String instanceId) {
		if (KNOWN_INSTANCES.containsKey(instanceId)) {
			int count = KNOWN_INSTANCES.get(instanceId);
			count++;
			KNOWN_INSTANCES.put(instanceId, count);
		} else {
			KNOWN_INSTANCES.put(instanceId, 1);
		}
	}

	/**
	 * Increase counter of visited VM. If VM is unknown, add to map with count 1.
	 *
	 * @param vmId ID of the visited Lambda VM.
	 */
	public synchronized static void reportVm(String vmId) {
		if (KNOWN_VMS.containsKey(vmId)) {
			int count = KNOWN_VMS.get(vmId);
			count++;
			KNOWN_VMS.put(vmId, count);
		} else {
			KNOWN_VMS.put(vmId, 1);
		}
	}
}
