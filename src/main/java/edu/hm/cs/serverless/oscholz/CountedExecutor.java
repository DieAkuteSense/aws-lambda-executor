package edu.hm.cs.serverless.oscholz;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Executor to call an AWS Lambda for x times. Save the IDs of the visited instances and VMs. Lambda invocations will
 * occur all [0,1] seconds, the time to wait will be chosse randomly.
 *
 * @author Oliver Scholz
 */
public class CountedExecutor implements Runnable {

	private final String lambdaArn;
	private final Regions awsRegion;
	private final int numberOfRuns;

	Random rand = new Random();

	public CountedExecutor(final String lambdaArn, final Regions awsRegion, final int numberOfRuns) {
		this.lambdaArn = lambdaArn;
		this.awsRegion = awsRegion;
		this.numberOfRuns = numberOfRuns;
	}

	public void run() {
		InvokeRequest invokeRequest = new InvokeRequest()
				.withFunctionName(lambdaArn);
		InvokeResult invokeResult = null;

		try {
			AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
					.withCredentials(new ProfileCredentialsProvider())
					.withRegion(awsRegion)
					.build();

			for (int i = 0; i < numberOfRuns; i++) {
				invokeResult = awsLambda.invoke(invokeRequest);
				Gson gson = new Gson();
				String ans = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
				Body body = gson.fromJson(ans, Body.class);

				String vmId = body.getBody().getVmInfo().getVmId();
				String instId = body.getBody().getVmInfo().getInstId();

				App.reportVm(vmId);
				App.reportInstance(instId);

				Thread.sleep(rand.nextInt(1000));
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}
