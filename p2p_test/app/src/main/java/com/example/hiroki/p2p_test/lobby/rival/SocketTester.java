package com.example.hiroki.p2p_test.lobby.rival;

import com.example.hiroki.p2p_test.battle.character.Battler;
import com.example.hiroki.p2p_test.battle.controller.ControllerBase;
import com.example.hiroki.p2p_test.battle.controller.SocketController;
import com.example.hiroki.p2p_test.lobby.p2p.AsyncSocket;
import com.example.hiroki.p2p_test.lobby.p2p.AuraBattleProtocol;
import com.example.hiroki.p2p_test.lobby.p2p.ClientSocket;
import com.example.hiroki.p2p_test.lobby.p2p.ServerSocket;

import java.util.Random;

import static com.example.hiroki.p2p_test.lobby.p2p.WifiSocketProtocol.SOCK_PORT;

/**
 * Created by hiroki on 2016/11/07.
 */

public class SocketTester extends RivalBase {
    private ClientSocket cs;
    private AuraBattleProtocol mABP;
    private Random r;
    private AsyncSocket mAcceptSocket;

    public SocketTester() {
        super("ソケットさん");
        r = new Random(System.currentTimeMillis());
    }

    public boolean requestBattle(String name) {
        if (getStatus() == ConnectionStatus.ALL_OK) {
            return true;    // 合意済み
        }
        if (getStatus() == ConnectionStatus.SEND_REQUEST) {
            return true;    // 接続中
        }

        // 相手の接続待ちをするソケット
        ServerSocket ss = new ServerSocket();
        ss.accept(SOCK_PORT, new ServerSocket.AcceptListener() {
            @Override
            public void onAccept(AsyncSocket as) {
                mAcceptSocket = as;
                dispatchBattleStatus();
            }

            @Override
            public void test(String e) {

            }
        });

        // 仮想の相手がこっちに接続するためのソケット
        cs = new ClientSocket();
        cs.connect("127.0.0.1", SOCK_PORT, new ClientSocket.ConnectListener(){
            @Override
            public void onConnect(boolean result) {
                // 内部にコマンドを送るプレイヤーを内包
                mABP = new AuraBattleProtocol(cs);
                mABP.setListener(new AuraBattleProtocol.Listener() {
                    @Override
                    public void onRecvAction(int act) {
                        mABP.sendDecideAction(r.nextInt(4) + 1);
                    }
                    @Override
                    public void onRecvResult(int turn_number, Battler.Status my_data, Battler.Status enemy_data) {
                        mABP.sendResult(turn_number, my_data, enemy_data);
                    }
                });
            }
        });

        return true;
    }

    @Override
    public void cancelBattle() {
        if (cs != null) {
            cs.close();
        }
        if (mAcceptSocket != null) {
            mAcceptSocket.close();
        }
        if (mABP!=null) {
            mABP.onClose();
        }
        cs = null;
        mAcceptSocket = null;
        mABP = null;
        dispatchBattleStatus();
    }

    @Override
    ConnectionStatus getStatus() {
        if (cs == null) {
            return ConnectionStatus.DISCONNECTED;
        }
        else if (mAcceptSocket == null) {
            return ConnectionStatus.SEND_REQUEST;
        }
        else {
            return ConnectionStatus.ALL_OK;
        }
    }

    @Override
    public ControllerBase createController() {
        return new SocketController(mAcceptSocket);
    }
}
