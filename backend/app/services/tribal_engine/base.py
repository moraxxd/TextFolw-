from abc import ABC, abstractmethod
from typing import Dict, List, Any, Tuple

class TribalLanguageAdapter(ABC):
    """
    Abstract Base Class for indigenous and tribal language adapters.
    Ensures modularity so that new tribal languages (Gondi, Kolami, Korku,
    Bhili, Santhali, Kui, etc.) can be plugged into the engine cleanly.
    """

    @property
    @abstractmethod
    def code(self) -> str:
        """ISO 639-3 or standardized language code."""
        pass

    @property
    @abstractmethod
    def display_name(self) -> str:
        """Standard English display name."""
        pass

    @property
    @abstractmethod
    def native_script_name(self) -> str:
        """Native script / local representation."""
        pass

    @property
    @abstractmethod
    def language_family(self) -> str:
        """Linguistic family (e.g. Dravidian, Austroasiatic)."""
        pass

    @property
    @abstractmethod
    def sub_branch(self) -> str:
        """Linguistic sub-branch (e.g. South-Central Dravidian, Central Dravidian)."""
        pass

    @property
    @abstractmethod
    def regions_spoken(self) -> List[str]:
        """Geographic regions where primarily spoken."""
        pass

    @abstractmethod
    def get_dataset_citations(self) -> List[Dict[str, str]]:
        """List of investigated corpora, academic datasets, and dictionaries."""
        pass

    @abstractmethod
    def get_honest_capability_statement(self) -> str:
        """Honest scientific disclosure of model capabilities and current limitations."""
        pass

    @abstractmethod
    def normalize_phonetics(self, input_text: str) -> str:
        """Normalize speech recognition phonetic output or Devanagari/Roman orthography."""
        pass

    @abstractmethod
    def parse_morphology(self, tokens: List[str]) -> List[Dict[str, Any]]:
        """Segment stems, grammatical cases (dative, genitive, accusative), and verb suffixes."""
        pass

    @abstractmethod
    def translate(self, text: str, target_lang: str) -> Tuple[str, float, Dict[str, Any]]:
        """
        Translates parsed tribal speech to English ('en') or Marathi ('mr').
        Returns:
            Tuple of (translated_text, confidence_score, linguistic_metadata)
        """
        pass
