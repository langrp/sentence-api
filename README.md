# Small API project

## Types:
#### Word
```json
{
  "id": "01A",
  "word": "foo",
  "category": "NOUN"
}
```
Possible categories:
* NOUN
* VERB
* ADJECTIVE

#### Sentence
```json
{
  "id": "ooxxx",
  "text": "kawa is best",
  "views": 1
}
```
#### Sentence Aggregate
```json
[{
  "text": "kawa is best",
  "sentenceIds": ["000xxx", "111xxx"]
}]
```

## Resources:
GET /words - list all words added
```json
[{
    "id": "000xxx",
    "word": "foo",
    "category": "NOUN"
  },{
    "id": "001xxx",
    "word": "boo",
    "category": "VERB"
  }]
```

PUT /words
```json
{
  "word": "foo",
  "category": "NOUN"
}
```

GET /words/{word}
```json
{
  "id": "000xxx",
  "word": "foo",
  "category": "NOUN"
}
```


GET /sentences - list all generated sentences
```json
[{
  "id": "000xxx",
  "text": "kawa is best",
  "views": 0
}]
```

POST /sentences/generate (call without request body)

* generate new sentence with unique id (accessible by GET /sentences/{sentenceID})
* add date + time when the sentence was generated
* optional 1. - number of views of the single sentence
* on resource /sentences/{sentenceID}  you can see the number of view of the specific sentence
* optional 2. (Advanced) Track number and id of exactly the same generated sentences (separate resource - introduce one)
* sentence should be random String in form of NOUN VERB ADJECTIVE only from words added to system
* if there is no all necessary types of words system should return error


GET /sentences/{sentenceID}
```json
{
  "id": "000xxx",
  "text": "kawa is best",
  "views": 1
}
```


GET /sentences/{sentenceID}/yodaTalk
* return special form of the requested sentence (ADJECTIVE NOUN VERB)
```json
{
  "id": "000xxx",
  "text": "best kawa is",
  "views": 1
}
```

GET /sentences/duplicates
* Returns all duplicated sentences
```json
[
  {
    "text": "kawa is best",
    "sentenceIds": ["000xxx", "001xxx"]
  }
]
```