package HospitalOpdQueueSys;

import java.time.LocalDateTime;

public class Token {

    public enum TokenStatus {
        ACTIVE,
        COMPLETED,
        EXPIRED,
        CANCELLED
    }

    private String tokenId;
    private String patientId;
    private String doctorId;
    private int tokenNumber;
    private LocalDateTime issuedAt;
    private TokenStatus status;

    public Token(String tokenId, String patientId,
                 String doctorId, int tokenNumber) {
        this.tokenId     = tokenId;
        this.patientId   = patientId;
        this.doctorId    = doctorId;
        this.tokenNumber = tokenNumber;
        this.issuedAt    = LocalDateTime.now();
        this.status      = TokenStatus.ACTIVE;
    }

    public String getTokenId()           { return tokenId; }
    public String getPatientId()         { return patientId; }
    public String getDoctorId()          { return doctorId; }
    public int getTokenNumber()          { return tokenNumber; }
    public LocalDateTime getIssuedAt()   { return issuedAt; }
    public TokenStatus getStatus()       { return status; }
    public void setStatus(TokenStatus s) { this.status = s; }

    @Override
    public String toString() {
        return String.format(
                "Token#%-4d | Patient:%-8s | Doctor:%-8s | Issued:%s | Status:%s",
                tokenNumber, patientId, doctorId, issuedAt, status
        );
    }
}