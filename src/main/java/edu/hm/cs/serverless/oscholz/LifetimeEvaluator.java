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
 * Evaluator to ping an AWS Lambda in specified time periods, recognize new lambda instance and determine the maximum
 * lifetime of a single lambda instance.
 *
 * @author Oliver Scholz
 */
public class LifetimeEvaluator {

	private static final long ONE_SECOND = 1000;
	private static final long ONE_MINUTE = ONE_SECOND * 60;
	private static final long ONE_HOUR = ONE_MINUTE * 60;

	public static void run(final String lambdaArn, final Regions awsRegion, final int pollingInterval) {
		InvokeRequest invokeRequest = new InvokeRequest()
				.withFunctionName(lambdaArn);
		InvokeResult invokeResult = null;

		long startTime = System.currentTimeMillis();
		int visited = 0;
		try {
			AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
					.withCredentials(new ProfileCredentialsProvider())
					.withRegion(awsRegion)
					.build();

			// First invocation: Determine current instance
			invokeResult = awsLambda.invoke(invokeRequest);
			Gson gson = new Gson();
			String ans = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
			Body body = gson.fromJson(ans, Body.class);

			String instId = body.getBody().getVmInfo().getInstId();

			System.out.println(body.toString());

			String currentInst = "";

			do {
				visited++;
				Thread.sleep(ONE_MINUTE * pollingInterval);

				invokeResult = awsLambda.invoke(invokeRequest);
				Gson repGson = new Gson();
				String repAns = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
				Body repBody = repGson.fromJson(repAns, Body.class);

				currentInst = repBody.getBody().getVmInfo().getInstId();
			} while (instId.equalsIgnoreCase(currentInst));

			System.out.println("Instance mismatch after " + (System.currentTimeMillis() - startTime) + " ms." +
					"Visited " + visited + " times.");

		} catch (InterruptedException e) {
			System.err.println(e);
		}
	}
}
