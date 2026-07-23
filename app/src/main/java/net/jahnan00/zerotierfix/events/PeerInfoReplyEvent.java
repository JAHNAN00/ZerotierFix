package net.jahnan00.zerotierfix.events;

import com.zerotier.sdk.Peer;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 应答结点信息事件
 *
 * @author jahnan00
 */
@Data
@AllArgsConstructor
public class PeerInfoReplyEvent {

    private final Peer[] peers;
}
