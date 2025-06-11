package vn.edu.iuh.fit.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollOption {
    private String optionText;
    private List<ObjectId> voters; // List of user IDs who voted for this option
}
