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
 * Executor to call an AWS Lambda for a time period of x hours. Save the IDs of the visited instances and VMs. Lambda
 * invocations will occur all [0,30] minutes, the time to wait will be choose randomly.
 *
 * @author Oliver Scholz
 */
public class DurableExecutor implements Runnable {

	private static final int ONE_SECOND = 1000;
	private static final int ONE_MINUTE = ONE_SECOND * 60;
	private static final int ONE_HOUR = ONE_MINUTE * 60;

	private final String lambdaArn;
	private final Regions awsRegion;
	Random rand = new Random();

	public DurableExecutor(final String lambdaArn, final Regions awsRegion) {
		this.lambdaArn = lambdaArn;
		this.awsRegion = awsRegion;
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

			long startTime = System.currentTimeMillis();

			int runCount = 0;
			do {
				runCount++;
				invokeResult = awsLambda.invoke(invokeRequest);
				Gson gson = new Gson();
				String ans = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
				Body body = gson.fromJson(ans, Body.class);

				String vmId = body.getBody().getVmInfo().getVmId();
				String instId = body.getBody().getVmInfo().getInstId();

				System.out.println(body.getBody().toString());

				App.reportVm(vmId);
				App.reportInstance(instId);

				Thread.sleep(rand.nextInt(30 * ONE_MINUTE));
			} while (System.currentTimeMillis() - startTime <= 4 * ONE_HOUR);
			System.out.println("Called " + runCount + " lambdas");
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}
