from typing import Dict, List, Optional
from .base import TribalLanguageAdapter
from .gondi_adapter import GondiAdapter
from .kolami_adapter import KolamiAdapter

class TribalEngineRegistry:
    """
    Central registry managing all active tribal language adapters.
    Engineered to allow future tribal languages (e.g., Korku, Bhili,
    Santhali, Kui) to be dynamically registered without refactoring.
    """

    def __init__(self):
        self._adapters: Dict[str, TribalLanguageAdapter] = {}
        # Pre-register our first-class supported tribal languages
        self.register(GondiAdapter())
        self.register(KolamiAdapter())

    def register(self, adapter: TribalLanguageAdapter) -> None:
        self._adapters[adapter.code.lower()] = adapter

    def get_adapter(self, lang_code: str) -> Optional[TribalLanguageAdapter]:
        return self._adapters.get(lang_code.lower())

    def is_tribal(self, lang_code: str) -> bool:
        return lang_code.lower() in self._adapters

    def list_adapters(self) -> List[TribalLanguageAdapter]:
        return list(self._adapters.values())

    def list_codes(self) -> List[str]:
        return list(self._adapters.keys())

# Global singleton registry instance
tribal_registry = TribalEngineRegistry()
