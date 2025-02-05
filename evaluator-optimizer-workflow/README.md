## Evaluator Optimizer Workflow

LLMs love to hallucinate and produce shitty responses. One solution is to evaluate the LLM response by an LLM itself, preferably a separate one.

This process can be automated using the [evaluator-optimizer workflow](https://www.anthropic.com/research/building-effective-agents#:~:text=Workflow%3A%20Evaluator%2Doptimizer), where we iteratively evaluate the primary LLM's response and provide it feedback if the evaluation fails, for up to _N_ iterations.

This proof of concept serves as a reference implementation of this workflow on top of a RAG chatbot.

<p align="center">
<img width="400" alt="evaluator-optimizer-workflow-diagram" src="https://github.com/user-attachments/assets/e56012ef-0abf-4377-80b0-7e20eba4653d" />
</p>

Spring AI defines the `Evaluator` interface to perform evaluation on LLM responses, however, the two implementations it provides are very rudimentary and are not fit to be used in an implementation of the evaluator-optimizer workflow.

Both of them work with a boolean pass/fail response and do not utilize the `feedback` and `score` attributes that we'll need in this workflow. For the same reason, we also can't use an evaluator LLM that only produces a yes/no response, like [`bespoke-minicheck`](https://www.bespokelabs.ai/bespoke-minicheck).

The POC uses a [custom implementation class](https://github.com/hardikSinghBehl/spring-ai-playground/tree/main/evaluator-optimizer-workflow/src/main/java/com/behl/LLMResponseEvaluator.java) built against this [system prompt](https://github.com/hardikSinghBehl/spring-ai-playground/tree/main/evaluator-optimizer-workflow/src/main/resources/prompts/evaluator-system-prompt.st).

## Configurations

The application uses local LLMs from [Ollama](https://ollama.com/), which are pulled automatically when the application starts.

The primary model, evaluator model, and the embedding model are configured in the [`application.yaml`](https://github.com/hardikSinghBehl/spring-ai-playground/tree/main/evaluator-optimizer-workflow/src/main/resources/application.yaml) file:

```yaml
spring:
  ai:
    ollama:
      chat:
        options:
          model: llama3.1
      embedding:
        options:
          model: nomic-embed-text

com:
  behl:
    evaluator:
      model: mistral
      max-iterations: 3
      acceptable-score: 0.8
```
We use a custom configuration property for our evaluator model, since Spring AI only allows to configure a single Ollama model currently. Additionally, the `max-iterations` and `acceptable-score` attributes are also externalized.

The proof of concept uses [`llama 3.1`](https://ollama.com/library/llama3.1) and [`mistral`](https://ollama.com/library/mistral) as the primary and evaluator models, respectively. Feel free to try the implementation with other models.

The prompts used by the evaluator-optimizer workflow are stored in the [`src/main/resources/prompts`](https://github.com/hardikSinghBehl/spring-ai-playground/tree/main/evaluator-optimizer-workflow/src/main/resources/prompts) directory, with their locations also defined in the `application.yaml` file:

```yaml
com:
  behl:
    evaluator:
      system-prompt: classpath:prompts/evaluator-system-prompt.st
      user-prompt: classpath:prompts/evaluator-user-prompt.st
    optimizer:
      feedback-prompt: classpath:prompts/optimization-feedback-prompt.st
```

The [`EvaluatorOptimizerProperties`](https://github.com/hardikSinghBehl/spring-ai-playground/tree/main/evaluator-optimizer-workflow/src/main/java/com/behl/EvaluatorOptimizerProperties.java) class encapsulates these custom properties in the codebase.

## Project Overview

During application startup, the [`VectorStoreInitializer`](https://github.com/hardikSinghBehl/spring-ai-playground/tree/main/evaluator-optimizer-workflow/src/main/java/com/behl/VectorStoreInitializer.java) class fetches the documents stored in [`src/main/resources/documents`](https://github.com/hardikSinghBehl/spring-ai-playground/tree/main/evaluator-optimizer-workflow/src/main/resources/documents) directory and populates the in-memory vector store using the configured embedding model.

When the sole `execute()` method of [`EvaluatorOptimizerWorkflow`](https://github.com/hardikSinghBehl/spring-ai-playground/tree/main/evaluator-optimizer-workflow/src/main/java/com/behl/EvaluatorOptimizerWorkflow.java) is called with a user query, it generates an initial response using the primary LLM. The response, along with the original query and context, are then evaluated by the evaluator model.

If the evaluation result is below the configured `acceptable-score`, the workflow [prepares a new prompt](https://github.com/hardikSinghBehl/spring-ai-playground/tree/main/evaluator-optimizer-workflow/src/main/resources/prompts/optimization-feedback-prompt.st) with the feedback from the evaluator model and sends it again to the primary LLM.

The process is repeated until the evaluation succeeds or the configured `max-iterations` limit is reached.

## Local Setup

Docker Compose can be used to spin up the Spring Boot application and Ollama service:

```
docker-compose build
```

```
docker-compose up -d
```

The application exposes a single POST API endpoint `/api/v1/chat` to interact with the RAG chatbot. The below [httpie](https://github.com/httpie/cli) command can be used to invoke it:

```bash
http POST :8080/api/v1/chat question="How many days sick leave can I take?"
```