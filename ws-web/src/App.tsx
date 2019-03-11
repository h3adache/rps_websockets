import React, { useEffect, useRef, useState } from "react";
import { Badge, Button, Card, CardBody, CardFooter, CardHeader, Col, Row } from "reactstrap";

const images = ["/assets/paper.png", "/assets/rock.png", "/assets/scissor.png"];
const actions = ["p", "r", "s"];
const outcomes: { [key: string]: any[] } = {
  "o|w": [1, "You Won!", "primary"],
  "o|l": [2, "You Lost :(", "danger"],
  "o|t": [0, "Tied Game!", "info"]
};

type ActionProps = { imgSrc: string; index: number; actionHandler: Function };

const ActionButton = (props: ActionProps) => {
  return (
    <Button onClick={() => props.actionHandler(props.index)}>
      <img src={props.imgSrc} width={64} height={64} />
    </Button>
  );
};

type OutcomeProps = {
  outcome: string;
};

const Outcome = ({outcome}: OutcomeProps) => {
  const outcomeMessage = (outcome == "") ? "" : outcomes[outcome][1];
  return (
    <h1>
      <Badge color="primary">{outcomeMessage}</Badge>
    </h1>
  );
};

export default () => {
  const [player1, setPlayer1Hand] = useState(0);
  const [player2, setPlayer2Hand] = useState(0);
  const [lastOutcome, setLastOutcome] = useState("");

  const playedRef = useRef(player1);
  const wsRef = useRef<WebSocket>();
  useEffect(() => {
    wsRef.current = new WebSocket(`ws://${window.location.hostname}:8080/game`);
    const ws = wsRef.current;
    ws.onmessage = (message:MessageEvent) => {
      const outcome: string = message.data;
      if(outcome.startsWith('o|')) {
        const offset = outcomes[outcome][0];
        setLastOutcome(outcome);
        setPlayer2Hand((playedRef.current + offset) % 3);
      }
    };
    ws.onopen = () => ws.send("p|r");
  }, []);

  const actionButtons = images.map((src: string, idx: number) => (
    <ActionButton
      key={idx}
      imgSrc={src}
      index={idx}
      actionHandler={(index: number) => {
        setLastOutcome("");
        playedRef.current = index;
        setPlayer1Hand(index);
        wsRef.current!.send("a|" + actions[index]);
      }}
    />
  ));

  return (
    <Card style={{ width: "100%" }}>
      <CardHeader>
        <Row>
          <Col>You</Col>
          <Col>Opponent</Col>
        </Row>
      </CardHeader>
      <CardBody>
        <Row>
          <Col>
            <img src={images[player1]} width={256} height={256} />
          </Col>
          <Col>
            <img src={images[player2]} width={256} height={256} />
          </Col>
        </Row>
        <Row>
          <Col>
            <Outcome outcome={lastOutcome} />
          </Col>
        </Row>
      </CardBody>
      <CardFooter>{actionButtons}</CardFooter>
    </Card>
  );
};
