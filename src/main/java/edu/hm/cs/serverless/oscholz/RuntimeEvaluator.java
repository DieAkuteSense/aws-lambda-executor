package edu.hm.cs.serverless.oscholz;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

/**
 * Evaluator to ping an AWS Lambda instance to determine the maximum time difference between Lambda invocations to get a
 * response of an already known Lambda instance.
 *
 * @author Oliver Scholz
 */
public class RuntimeEvaluator {

	private static final long ONE_SECOND = 1000;
	private static final long ONE_MINUTE = ONE_SECOND * 60;
	private static final long ONE_HOUR = ONE_MINUTE * 60;


	public static void determine(final String lambdaArn, final Regions awsRegion) {
		long startTime = System.currentTimeMillis();

		InvokeRequest invokeRequest = new InvokeRequest()
				.withFunctionName(lambdaArn);

		try {
			AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
					.withCredentials(new ProfileCredentialsProvider())
					.withRegion(awsRegion)
					.build();


			int runCount = 0;
			String instanceToRecognize = "";
			String instId = "";
			while (instanceToRecognize.equalsIgnoreCase(instId)) {
				System.out.println("Current run: " + runCount + "; Sleep-Time: " + getWaitTime(2, runCount) + " minutes");
				Thread.sleep(getWaitTime(2, runCount) * ONE_MINUTE);
				InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
				String ans = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
				Body body = new Gson().fromJson(ans, Body.class);

				instId = body.getBody().getVmInfo().getInstId();

				// first run: set instance id as "current instance"
				if (runCount == 0) {
					instanceToRecognize = instId;
				}

				if (instanceToRecognize.equalsIgnoreCase(instId)) {
					runCount++;
				}
			}
			;

			System.out.println("Mismatch! Starting Divide&Conquer!");

			double maxWaitTimeInMinutes = getWaitTime(2, runCount);
			double minWaitTimeInMinutes = getWaitTime(2, runCount - 1);

			String currentInstance;
			while ((maxWaitTimeInMinutes - minWaitTimeInMinutes) > 1) {
				double currentWaitTime = (minWaitTimeInMinutes + maxWaitTimeInMinutes) / 2;
				System.out.println("Divide&Conquer: Current Wait Time: " + currentWaitTime + " minutes");
				Thread.sleep((long) currentWaitTime * ONE_MINUTE);

				InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
				String ans = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
				Body body = new Gson().fromJson(ans, Body.class);

				currentInstance = body.getBody().getVmInfo().getInstId();
				if (instanceToRecognize.equalsIgnoreCase(currentInstance)) {
					minWaitTimeInMinutes = currentWaitTime;
				} else {
					instanceToRecognize = currentInstance;
					maxWaitTimeInMinutes = currentWaitTime;
				}

				if ((maxWaitTimeInMinutes - minWaitTimeInMinutes) <= 1) {
					System.out.println("Min and max wait time diff <= 1 minutes. Current wait time: " + currentWaitTime
							+ " minutes. Exiting...");
				}
			}
		} catch (InterruptedException e) {
			System.err.println(e);
		}

		System.out.println("App ran " + ((System.currentTimeMillis() - startTime) / 1000) + " secounds.");
	}

	private static long getWaitTime(int number, int power) {
		if (power == 0) {
			return 1;
		}

		int result = number;

		while (power > 1) {
			result *= number;
			power--;
		}

		return result;
	}
}
