package edu.bu.jgram.server.security;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

import edu.bu.jgram.server.assessment.Checkpoint;
import edu.bu.jgram.server.assessment.Result;
import io.jsonwebtoken.*;

import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;

public class JWT {

    private static final String CLAIM_PROP_TOTAL_CHECKPOINT = "TotalCheckpoint";
    private static final String CLAIM_PROP_GRADE_SUFFIX = "-Grade";
    private static final String CLAIM_PROP_WEIGHT_SUFFIX = "-Weight";
    private static final String CLAIM_PROP_FEEDBACK_SUFFIX = "-Feedback";
    private static final String CLAIM_PROP_OVERALL_GRADE = "OverallGrade";

    private final String mSecret;

    public JWT(String pSecret) {
        mSecret = pSecret;
    }

    public String create(String pId, String pIssuer, String pSubject, Result pResult) {

        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = mSecret.getBytes();
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder().setId(pId)
                .setIssuedAt(now)
                .setSubject(pSubject)
                .setIssuer(pIssuer)
                .signWith(signatureAlgorithm, signingKey);

        for (int checkpointID : pResult.getCheckpointMap().keySet()) {
            Checkpoint checkpoint = pResult.getCheckpointMap().get(checkpointID);

            builder.claim(checkpointID + CLAIM_PROP_GRADE_SUFFIX , checkpoint.getGrade());
            builder.claim(checkpointID + CLAIM_PROP_WEIGHT_SUFFIX , checkpoint.getWeight());
            builder.claim(checkpointID + CLAIM_PROP_FEEDBACK_SUFFIX , checkpoint.getFeedback());
        }

        builder.claim(CLAIM_PROP_TOTAL_CHECKPOINT, pResult.getCheckpointMap().size());
        builder.claim(CLAIM_PROP_OVERALL_GRADE, pResult.getOverallGrade());

        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    public Result decodeJWT(String pJwt) throws SecurityException {

        Result result = new Result();
        try {
            //This line will throw an exception if it is not a signed JWS (as expected)
            Claims claims = Jwts.parser()
                    .setSigningKey(mSecret.getBytes())
                    .parseClaimsJws(pJwt).getBody();

            int totalCheckpoints = (Integer) claims.get(CLAIM_PROP_TOTAL_CHECKPOINT);

             for (int i=1; i <= totalCheckpoints; i++) {
                 int grade = (Integer) claims.get(i + CLAIM_PROP_GRADE_SUFFIX);
                 int weight = (Integer) claims.get(i + CLAIM_PROP_WEIGHT_SUFFIX);
                 String feedback = (String) claims.get(i + CLAIM_PROP_FEEDBACK_SUFFIX);

                 result.addCheckpoint(new Checkpoint(weight, grade, feedback));
             }

            float overallGrade = Float.parseFloat(claims.get(CLAIM_PROP_OVERALL_GRADE).toString());
            result.setOverallGrade(overallGrade);

            return result;
        } catch (Exception e) {
            throw new SecurityException("Invalid token. Potential cause (1) The token has been tampered or (2) Incorrect secret");
        }
    }
}