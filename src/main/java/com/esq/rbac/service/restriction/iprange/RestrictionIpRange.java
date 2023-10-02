package com.esq.rbac.service.restriction.iprange;
import lombok.*;
import java.net.InetAddress;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RestrictionIpRange {
    private InetAddress address;
    private int blockSize;

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.address != null ? this.address.hashCode() : 0);
        hash = 37 * hash + this.blockSize;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RestrictionIpRange other = (RestrictionIpRange) obj;
        if (this.address != other.address && (this.address == null || !this.address.equals(other.address))) {
            return false;
        }
        if (this.blockSize != other.blockSize) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RestrictionIpRange{address=").append(address);
        sb.append("; blockSize=").append(blockSize);
        sb.append('}');
        return sb.toString();
    }
}

